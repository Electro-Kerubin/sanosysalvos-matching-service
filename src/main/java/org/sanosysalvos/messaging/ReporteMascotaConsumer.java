package org.sanosysalvos.messaging;

import java.util.List;
import org.sanosysalvos.client.GeolocationClient;
import org.sanosysalvos.dto.CoordenadaResponseDto;
import org.sanosysalvos.dto.ReporteMascotaEventDto;
import org.sanosysalvos.model.ReporteMascota;
import org.sanosysalvos.repository.CoincidenciaResultRepository;
import org.sanosysalvos.repository.ReporteMascotaRepository;
import org.sanosysalvos.service.CircuitBreakerService;
import org.sanosysalvos.service.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumidor RabbitMQ del Matching Service.
 *
 * Flujo al recibir un reporte nuevo desde el reporte-service:
 *  1. Sincroniza el ReporteMascota local (raza, color, tamano, tipoReporte, fechaReporte).
 *  2. [CircuitBreaker] Llama al geolocation-service vía Feign para obtener lat/lon.
 *  3. [CircuitBreaker] Busca reportes del tipo opuesto y ejecuta el algoritmo de matching.
 *  4. Si hay COINCIDENCIA_ALTA, publica la coincidencia a RabbitMQ para notificaciones.
 */
@Component
public class ReporteMascotaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReporteMascotaConsumer.class);

    private final ReporteMascotaRepository reporteMascotaRepository;
    private final CoincidenciaResultRepository coincidenciaResultRepository;
    private final GeolocationClient geolocationClient;
    private final CircuitBreakerService circuitBreakerService;
    private final MatchingService matchingService;

    public ReporteMascotaConsumer(
            ReporteMascotaRepository reporteMascotaRepository,
            CoincidenciaResultRepository coincidenciaResultRepository,
            GeolocationClient geolocationClient,
            CircuitBreakerService circuitBreakerService,
            MatchingService matchingService
    ) {
        this.reporteMascotaRepository = reporteMascotaRepository;
        this.coincidenciaResultRepository = coincidenciaResultRepository;
        this.geolocationClient = geolocationClient;
        this.circuitBreakerService = circuitBreakerService;
        this.matchingService = matchingService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    @Transactional
    public void recibirReporte(ReporteMascotaEventDto evento) {
        if (evento == null || evento.getIdReporteMascota() == null) {
            log.warn("[RabbitMQ] Mensaje recibido nulo o sin idReporteMascota, ignorando.");
            return;
        }

        log.info("[RabbitMQ] Mensaje recibido: idReporteMascota={}, tipo={}",
                evento.getIdReporteMascota(), evento.getDescripcionTipoReporte());

        // ── Paso 1: Sincronizar datos básicos del reporte ─────────────────────
        ReporteMascota reporte = reporteMascotaRepository
                .findById(evento.getIdReporteMascota().longValue())
                .orElse(new ReporteMascota());

        reporte.setIdReporteMascota(evento.getIdReporteMascota().longValue());
        reporte.setRaza(evento.getRaza());
        reporte.setTamano(evento.getTamano());
        reporte.setFechaReporte(evento.getFechaReporte());
        reporte.setIdTipoReporte(evento.getIdTipoReporte());

        String color = evento.getColorPrimario();
        if (color != null && evento.getColorSecundario() != null && !evento.getColorSecundario().isBlank()) {
            color = color + " / " + evento.getColorSecundario();
        }
        reporte.setColor(color);

        // Coordenadas del evento (pueden venir nulas desde el reporte-service)
        reporte.setLatitud(evento.getLatitud());
        reporte.setLongitud(evento.getLongitud());

        reporteMascotaRepository.save(reporte);
        log.info("[RabbitMQ] ReporteMascota id={} sincronizado (paso 1/4).", reporte.getIdReporteMascota());

        // ── Paso 2: Enriquecer con coordenadas del geolocation-service ────────
        final Long idReporte = reporte.getIdReporteMascota();
        try {
            circuitBreakerService.executeProtected(() -> {
                CoordenadaResponseDto coord = geolocationClient.getCoordenadaByReporte(idReporte);
                if (coord != null) {
                    reporte.setLatitud(coord.getUbicacionLat());
                    reporte.setLongitud(coord.getUbicacionLon());
                    reporteMascotaRepository.save(reporte);
                    log.info("[Geolocation] Coordenadas para reporte id={}: lat={}, lon={}",
                            idReporte, coord.getUbicacionLat(), coord.getUbicacionLon());
                }
            });
        } catch (Exception ex) {
            log.warn("[Geolocation] No se pudieron obtener coordenadas para reporte id={}: {}. " +
                    "Continuando con lat/lon del evento (pueden ser null).", idReporte, ex.getMessage());
        }

        // ── Paso 3: Buscar coincidencias con reportes del tipo opuesto ────────
        if (reporte.getIdTipoReporte() == null) {
            log.warn("[Matching] idTipoReporte es null para reporte id={}, no se puede buscar tipo opuesto.", idReporte);
            return;
        }

        List<ReporteMascota> reportesOpuestos = reporteMascotaRepository
                .findByIdTipoReporteNot(reporte.getIdTipoReporte());

        if (reportesOpuestos.isEmpty()) {
            log.info("[Matching] No hay reportes del tipo opuesto para comparar con id={}.", idReporte);
            return;
        }

        log.info("[Matching] Procesando {} reporte(s) del tipo opuesto contra id={}.",
                reportesOpuestos.size(), idReporte);

        for (ReporteMascota opuesto : reportesOpuestos) {
            try {
                // idTipoReporte = 1 → PERDIDO, idTipoReporte = 2 → ENCONTRADO (ajustar según BD)
                final Long idPerdido;
                final Long idEncontrado;

                if (reporte.getIdTipoReporte() == 1) {
                    idPerdido = reporte.getIdReporteMascota();
                    idEncontrado = opuesto.getIdReporteMascota();
                } else {
                    idPerdido = opuesto.getIdReporteMascota();
                    idEncontrado = reporte.getIdReporteMascota();
                }

                circuitBreakerService.executeProtected(() -> {
                    var solicitud = matchingService.solicitarCoincidencia(idPerdido, idEncontrado);
                    var resultado = matchingService.procesarCoincidencia(solicitud.idCoincidenciaRequest());

                    log.info("[Matching] perdido={} vs encontrado={}: veredicto={}",
                            idPerdido, idEncontrado, resultado.veredictoFinal());

                    // ── Paso 4: Publicar coincidencia alta a RabbitMQ ─────────
                    if ("COINCIDENCIA_ALTA".equals(resultado.veredictoFinal())) {
                        matchingService.coincidenciaPotencialEncontrada(solicitud.idCoincidenciaRequest());
                        log.info("[Matching] Coincidencia alta publicada para request id={}.",
                                solicitud.idCoincidenciaRequest());
                    }
                });

            } catch (Exception ex) {
                log.error("[Matching] Error procesando par (reporte={}, opuesto={}): {}",
                        idReporte, opuesto.getIdReporteMascota(), ex.getMessage());
            }
        }

        log.info("[RabbitMQ] Flujo completo finalizado para reporte id={}.", idReporte);
    }
}
