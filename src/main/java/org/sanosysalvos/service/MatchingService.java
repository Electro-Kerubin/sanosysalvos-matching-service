package org.sanosysalvos.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sanosysalvos.client.GeolocationClient;
import org.sanosysalvos.client.ReportesClient;
import org.sanosysalvos.dto.CoincidenciaConReporteDto;
import org.sanosysalvos.dto.CoincidenciaResultadoResponseDto;
import org.sanosysalvos.dto.CoincidenciaSolicitudResponseDto;
import org.sanosysalvos.dto.CoordenadaResponseDto;
import org.sanosysalvos.dto.MascotaDto;
import org.sanosysalvos.dto.NotificacionResultadoDto;
import org.sanosysalvos.dto.ReglaCoincidenciaResponseDto;
import org.sanosysalvos.dto.ReporteDto;
import org.sanosysalvos.exception.BusinessException;
import org.sanosysalvos.exception.NotFoundException;
import org.sanosysalvos.model.CoincidenciaRequest;
import org.sanosysalvos.model.CoincidenciaResult;
import org.sanosysalvos.model.CoincidenciaStatus;
import org.sanosysalvos.model.ReglaCoincidencia;
import org.sanosysalvos.model.ReporteMascota;
import org.sanosysalvos.repository.CoincidenciaRequestRepository;
import org.sanosysalvos.repository.CoincidenciaResultRepository;
import org.sanosysalvos.repository.CoincidenciaStatusRepository;
import org.sanosysalvos.repository.ReglaCoincidenciaRepository;
import org.sanosysalvos.repository.ReporteMascotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_PROCESADO = "PROCESADO";

    private final CoincidenciaRequestRepository coincidenciaRequestRepository;
    private final CoincidenciaResultRepository coincidenciaResultRepository;
    private final CoincidenciaStatusRepository coincidenciaStatusRepository;
    private final ReglaCoincidenciaRepository reglaCoincidenciaRepository;
    private final ReporteMascotaRepository reporteMascotaRepository;
    private final ScoringService scoringService;
    private final CircuitBreakerService circuitBreakerService;
    private final CoincidenciaNotifier coincidenciaNotifier;
    private final ReportesClient reportesClient;
    private final GeolocationClient geolocationClient;

    public MatchingService(
            CoincidenciaRequestRepository coincidenciaRequestRepository,
            CoincidenciaResultRepository coincidenciaResultRepository,
            CoincidenciaStatusRepository coincidenciaStatusRepository,
            ReglaCoincidenciaRepository reglaCoincidenciaRepository,
            ReporteMascotaRepository reporteMascotaRepository,
            ScoringService scoringService,
            CircuitBreakerService circuitBreakerService,
            CoincidenciaNotifier coincidenciaNotifier,
            ReportesClient reportesClient,
            GeolocationClient geolocationClient
    ) {
        this.coincidenciaRequestRepository = coincidenciaRequestRepository;
        this.coincidenciaResultRepository = coincidenciaResultRepository;
        this.coincidenciaStatusRepository = coincidenciaStatusRepository;
        this.reglaCoincidenciaRepository = reglaCoincidenciaRepository;
        this.reporteMascotaRepository = reporteMascotaRepository;
        this.scoringService = scoringService;
        this.circuitBreakerService = circuitBreakerService;
        this.coincidenciaNotifier = coincidenciaNotifier;
        this.reportesClient = reportesClient;
        this.geolocationClient = geolocationClient;
    }

    @Transactional
    public CoincidenciaSolicitudResponseDto solicitarCoincidencia(Long idPerdidoReporte, Long idEncontradoReporte) {
        ReporteMascota reportePerdido = fetchOrSeed(idPerdidoReporte);
        ReporteMascota reporteEncontrado = fetchOrSeed(idEncontradoReporte);

        CoincidenciaRequest request = new CoincidenciaRequest();
        request.setReportePerdido(reportePerdido);
        request.setReporteEncontrado(reporteEncontrado);
        request.setStatus(getStatus(ESTADO_PENDIENTE));
        request.setRequestedAt(LocalDateTime.now());

        CoincidenciaRequest saved = coincidenciaRequestRepository.save(request);
        return toSolicitudResponse(saved);
    }

    @Transactional
    public CoincidenciaResultadoResponseDto procesarCoincidencia(Long idCoincidenciaRequest) {
        CoincidenciaRequest request = coincidenciaRequestRepository.findById(idCoincidenciaRequest)
                .orElseThrow(() -> new NotFoundException("No existe coincidencia_request con id " + idCoincidenciaRequest));

        if (coincidenciaResultRepository.findByCoincidenciaRequest_IdCoincidenciaRequest(idCoincidenciaRequest).isPresent()) {
            throw new BusinessException("La solicitud ya tiene un resultado procesado");
        }

        List<ReglaCoincidencia> reglasActivas = reglaCoincidenciaRepository.findByIsActiveTrue();
        if (reglasActivas.isEmpty()) {
            throw new BusinessException("No existen reglas activas para procesar la coincidencia");
        }

        Map<String, BigDecimal> pesos = reglasToWeightMap(reglasActivas);
        BigDecimal pesoRaza = pesos.getOrDefault("raza", BigDecimal.ZERO);
        BigDecimal pesoColor = pesos.getOrDefault("color", BigDecimal.ZERO);
        BigDecimal pesoTamano = pesos.getOrDefault("tamano", BigDecimal.ZERO);
        BigDecimal pesoDistancia = pesos.getOrDefault("distancia", BigDecimal.ZERO);
        BigDecimal pesoFecha = pesos.getOrDefault("fecha", BigDecimal.ZERO);

        BigDecimal puntajeRaza = scoringService.scoreRaza(request.getReportePerdido(), request.getReporteEncontrado());
        BigDecimal puntajeColor = scoringService.scoreColor(request.getReportePerdido(), request.getReporteEncontrado());
        BigDecimal puntajeTamano = scoringService.scoreTamano(request.getReportePerdido(), request.getReporteEncontrado());
        BigDecimal puntajeDistancia = scoringService.scoreDistancia(request.getReportePerdido(), request.getReporteEncontrado());
        BigDecimal puntajeFecha = scoringService.scoreFecha(request.getReportePerdido(), request.getReporteEncontrado());

        BigDecimal puntajeTotal = scoringService.calcularPuntajeTotal(
                puntajeRaza,
                puntajeColor,
                puntajeTamano,
                puntajeDistancia,
                puntajeFecha,
                pesoRaza,
                pesoColor,
                pesoTamano,
                pesoDistancia,
                pesoFecha
        );

        CoincidenciaResult result = new CoincidenciaResult();
        result.setCoincidenciaRequest(request);
        result.setPuntajeRaza(puntajeRaza);
        result.setPuntajeColor(puntajeColor);
        result.setPuntajeTamano(puntajeTamano);
        result.setPuntajeDistancia(puntajeDistancia);
        result.setPuntajeFecha(puntajeFecha);
        result.setPuntajeTotal(puntajeTotal);
        result.setVeredictoFinal(scoringService.veredicto(puntajeTotal));
        result.setCreatedAt(LocalDateTime.now());

        CoincidenciaResult savedResult = coincidenciaResultRepository.save(result);

        request.setStatus(getStatus(ESTADO_PROCESADO));
        request.setProcessedAt(LocalDateTime.now());
        coincidenciaRequestRepository.save(request);

        return toResultadoResponse(savedResult);
    }

    @Transactional(readOnly = true)
    public CoincidenciaResultadoResponseDto obtenerResultadoCoincidencia(Long idCoincidenciaRequest) {
        CoincidenciaResult result = coincidenciaResultRepository.findByCoincidenciaRequest_IdCoincidenciaRequest(idCoincidenciaRequest)
                .orElseThrow(() -> new NotFoundException("No existe resultado para la solicitud " + idCoincidenciaRequest));
        return toResultadoResponse(result);
    }

    @Transactional(readOnly = true)
    public List<CoincidenciaConReporteDto> listarCoincidenciasPorReporte(Long idReporteMascota) {
        List<CoincidenciaRequest> requests = coincidenciaRequestRepository
                .findByReportePerdido_IdReporteMascotaOrReporteEncontrado_IdReporteMascota(idReporteMascota, idReporteMascota);

        return requests.stream()
                .map(CoincidenciaRequest::getIdCoincidenciaRequest)
                .map(coincidenciaResultRepository::findByCoincidenciaRequest_IdCoincidenciaRequest)
                .flatMap(java.util.Optional::stream)
                .map(this::toCoincidenciaConReporteDto)
                .toList();
    }

    public void sincronizarReporte(Long idReporte) {
        fetchOrSeed(idReporte);
    }

    @Transactional
    public CoincidenciaSolicitudResponseDto actualizarEstadoCoincidencia(Long idCoincidenciaRequest, String nuevoEstado) {
        CoincidenciaRequest request = coincidenciaRequestRepository.findById(idCoincidenciaRequest)
                .orElseThrow(() -> new NotFoundException("No existe coincidencia_request con id " + idCoincidenciaRequest));

        request.setStatus(getStatus(nuevoEstado.toUpperCase()));
        CoincidenciaRequest updated = coincidenciaRequestRepository.save(request);
        return toSolicitudResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ReglaCoincidenciaResponseDto> obtenerReglasActivas() {
        return reglaCoincidenciaRepository.findByIsActiveTrue().stream()
                .map(rule -> new ReglaCoincidenciaResponseDto(
                        rule.getIdReglasCoincidencias(),
                        rule.getDescripcion(),
                        rule.getImportancia(),
                        rule.getIsActive()
                ))
                .toList();
    }

    @Transactional
    public NotificacionResultadoDto coincidenciaPotencialEncontrada(Long idCoincidenciaRequest) {
        CoincidenciaResult result = coincidenciaResultRepository.findByCoincidenciaRequest_IdCoincidenciaRequest(idCoincidenciaRequest)
                .orElseThrow(() -> new NotFoundException("No existe resultado para la solicitud " + idCoincidenciaRequest));

        if (!"COINCIDENCIA_ALTA".equals(result.getVeredictoFinal())) {
            throw new BusinessException("La coincidencia no cumple umbral para notificacion potencial");
        }

        String state = circuitBreakerService.executeProtected(
                () -> coincidenciaNotifier.notificarCoincidenciaPotencial(idCoincidenciaRequest, result.getVeredictoFinal())
        );

        return new NotificacionResultadoDto(idCoincidenciaRequest, state, "Notificacion enviada correctamente");
    }

    private ReporteMascota fetchOrSeed(Long idReporte) {
        ReporteMascota reporte = reporteMascotaRepository.findById(idReporte).orElseGet(() -> {
            ReporteMascota r = new ReporteMascota();
            r.setIdReporteMascota(idReporte);
            return r;
        });

        boolean needsUpdate = reporte.getRaza() == null
                || reporte.getColor() == null
                || reporte.getTamano() == null
                || reporte.getFechaReporte() == null;

        if (needsUpdate) {
            log.info("[MatchingService] Sincronizando datos para reporte id={}.", idReporte);
            try {
                ReporteDto dto = reportesClient.getReporte(idReporte);
                if (dto != null) {
                    if (reporte.getIdTipoReporte() == null) reporte.setIdTipoReporte(dto.getIdTipoReporte());
                    if (reporte.getFechaReporte() == null) reporte.setFechaReporte(dto.resolveFechaEfectiva());
                    if (reporte.getRaza() == null && dto.getDescripcionRaza() != null) reporte.setRaza(dto.getDescripcionRaza());
                    if (reporte.getColor() == null && dto.getColorPrimario() != null) {
                        String color = dto.getColorPrimario();
                        if (dto.getColorSecundario() != null && !dto.getColorSecundario().isBlank()) {
                            color = color + " / " + dto.getColorSecundario();
                        }
                        reporte.setColor(color);
                    }
                    if (reporte.getTamano() == null && dto.getTamano() != null) reporte.setTamano(dto.getTamano());
                }
            } catch (Exception ex) {
                log.warn("[MatchingService] No se pudo sincronizar reporte id={}: {}", idReporte, ex.getMessage());
            }
            reporte = reporteMascotaRepository.save(reporte);
        }

        if (reporte.getLatitud() == null || reporte.getLongitud() == null) {
            try {
                CoordenadaResponseDto coord = geolocationClient.getCoordenadaByReporte(idReporte);
                if (coord != null) {
                    reporte.setLatitud(coord.getUbicacionLat());
                    reporte.setLongitud(coord.getUbicacionLon());
                    reporte = reporteMascotaRepository.save(reporte);
                }
            } catch (Exception ex) {
                log.warn("[MatchingService] No se pudieron obtener coordenadas para reporte id={}: {}", idReporte, ex.getMessage());
            }
        }

        return reporte;
    }

    private CoincidenciaStatus getStatus(String descripcion) {
        return coincidenciaStatusRepository.findByDescripcion(descripcion)
                .orElseThrow(() -> new NotFoundException("No existe coincidencia_status: " + descripcion));
    }

    private Map<String, BigDecimal> reglasToWeightMap(List<ReglaCoincidencia> reglasActivas) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (ReglaCoincidencia regla : reglasActivas) {
            String key = normalizeRuleKey(regla.getDescripcion());
            map.put(key, regla.getImportancia());
        }
        return map;
    }

    private String normalizeRuleKey(String key) {
        if (key == null) {
            return "";
        }
        String normalized = Normalizer.normalize(key.trim().toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private CoincidenciaSolicitudResponseDto toSolicitudResponse(CoincidenciaRequest request) {
        return new CoincidenciaSolicitudResponseDto(
                request.getIdCoincidenciaRequest(),
                request.getReportePerdido().getIdReporteMascota(),
                request.getReporteEncontrado().getIdReporteMascota(),
                request.getStatus().getDescripcion(),
                request.getRequestedAt(),
                request.getProcessedAt()
        );
    }

    private CoincidenciaResultadoResponseDto toResultadoResponse(CoincidenciaResult result) {
        return new CoincidenciaResultadoResponseDto(
                result.getIdCoincidenciaResultado(),
                result.getCoincidenciaRequest().getIdCoincidenciaRequest(),
                result.getPuntajeTotal(),
                result.getPuntajeRaza(),
                result.getPuntajeColor(),
                result.getPuntajeTamano(),
                result.getPuntajeDistancia(),
                result.getPuntajeFecha(),
                result.getVeredictoFinal(),
                result.getCreatedAt()
        );
    }

    private CoincidenciaConReporteDto toCoincidenciaConReporteDto(CoincidenciaResult result) {
        CoincidenciaRequest req = result.getCoincidenciaRequest();
        return new CoincidenciaConReporteDto(
                result.getIdCoincidenciaResultado(),
                req.getIdCoincidenciaRequest(),
                req.getReportePerdido().getIdReporteMascota(),
                req.getReporteEncontrado().getIdReporteMascota(),
                result.getPuntajeTotal(),
                result.getPuntajeRaza(),
                result.getPuntajeColor(),
                result.getPuntajeTamano(),
                result.getPuntajeDistancia(),
                result.getPuntajeFecha(),
                result.getVeredictoFinal(),
                result.getCreatedAt()
        );
    }
}


