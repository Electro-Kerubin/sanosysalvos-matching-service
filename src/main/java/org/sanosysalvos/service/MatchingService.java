package org.sanosysalvos.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sanosysalvos.dto.CoincidenciaResultadoResponseDto;
import org.sanosysalvos.dto.CoincidenciaSolicitudResponseDto;
import org.sanosysalvos.dto.NotificacionResultadoDto;
import org.sanosysalvos.dto.ReglaCoincidenciaResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingService {

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

    public MatchingService(
            CoincidenciaRequestRepository coincidenciaRequestRepository,
            CoincidenciaResultRepository coincidenciaResultRepository,
            CoincidenciaStatusRepository coincidenciaStatusRepository,
            ReglaCoincidenciaRepository reglaCoincidenciaRepository,
            ReporteMascotaRepository reporteMascotaRepository,
            ScoringService scoringService,
            CircuitBreakerService circuitBreakerService,
            CoincidenciaNotifier coincidenciaNotifier
    ) {
        this.coincidenciaRequestRepository = coincidenciaRequestRepository;
        this.coincidenciaResultRepository = coincidenciaResultRepository;
        this.coincidenciaStatusRepository = coincidenciaStatusRepository;
        this.reglaCoincidenciaRepository = reglaCoincidenciaRepository;
        this.reporteMascotaRepository = reporteMascotaRepository;
        this.scoringService = scoringService;
        this.circuitBreakerService = circuitBreakerService;
        this.coincidenciaNotifier = coincidenciaNotifier;
    }

    @Transactional
    public CoincidenciaSolicitudResponseDto solicitarCoincidencia(Long idPerdidoReporte, Long idEncontradoReporte) {
        ReporteMascota reportePerdido = reporteMascotaRepository.findById(idPerdidoReporte)
                .orElseThrow(() -> new NotFoundException("No existe reporte perdido con id " + idPerdidoReporte));

        ReporteMascota reporteEncontrado = reporteMascotaRepository.findById(idEncontradoReporte)
                .orElseThrow(() -> new NotFoundException("No existe reporte encontrado con id " + idEncontradoReporte));

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
    public List<CoincidenciaResultadoResponseDto> listarCoincidenciasPorReporte(Long idReporteMascota) {
        List<CoincidenciaRequest> requests = coincidenciaRequestRepository
                .findByReportePerdido_IdReporteMascotaOrReporteEncontrado_IdReporteMascota(idReporteMascota, idReporteMascota);

        return requests.stream()
                .map(CoincidenciaRequest::getIdCoincidenciaRequest)
                .map(coincidenciaResultRepository::findByCoincidenciaRequest_IdCoincidenciaRequest)
                .flatMap(java.util.Optional::stream)
                .map(this::toResultadoResponse)
                .toList();
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
}


