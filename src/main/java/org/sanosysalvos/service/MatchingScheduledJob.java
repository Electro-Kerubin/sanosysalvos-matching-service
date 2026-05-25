package org.sanosysalvos.service;

import java.util.List;
import org.sanosysalvos.client.ReportesClient;
import org.sanosysalvos.dto.ReporteDto;
import org.sanosysalvos.repository.CoincidenciaRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ejecuta matching automático entre todos los reportes perdidos y encontrados.
 * Corre cada 10 minutos. Solo procesa pares que aún no tienen una solicitud.
 */
@Component
public class MatchingScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(MatchingScheduledJob.class);

    private final ReportesClient reportesClient;
    private final MatchingService matchingService;
    private final CoincidenciaRequestRepository coincidenciaRequestRepository;

    public MatchingScheduledJob(
            ReportesClient reportesClient,
            MatchingService matchingService,
            CoincidenciaRequestRepository coincidenciaRequestRepository
    ) {
        this.reportesClient = reportesClient;
        this.matchingService = matchingService;
        this.coincidenciaRequestRepository = coincidenciaRequestRepository;
    }

    // Corre 60 s después del arranque, luego cada 10 minutos
    @Scheduled(initialDelay = 60000, fixedDelay = 600000)
    public void ejecutarMatchingAutomatico() {
        log.info("[MatchingJob] Iniciando ciclo de matching automático.");
        try {
            List<ReporteDto> todos = reportesClient.getAllReportes();
            if (todos == null || todos.isEmpty()) {
                log.info("[MatchingJob] No hay reportes disponibles.");
                return;
            }

            // Seed local para reportes que aún no están en la tabla
            todos.forEach(dto -> {
                if (dto.getIdReporteMascota() != null) {
                    matchingService.sincronizarReporte(dto.getIdReporteMascota().longValue());
                }
            });

            // idTipoReporte = 1 → PERDIDO, 2 → ENCONTRADO (según convención de la BD)
            List<ReporteDto> perdidos   = todos.stream().filter(r -> Integer.valueOf(1).equals(r.getIdTipoReporte())).toList();
            List<ReporteDto> encontrados = todos.stream().filter(r -> Integer.valueOf(2).equals(r.getIdTipoReporte())).toList();

            log.info("[MatchingJob] {} reporte(s) perdido(s) × {} reporte(s) encontrado(s) = {} par(es) a evaluar.",
                    perdidos.size(), encontrados.size(), perdidos.size() * encontrados.size());

            int procesados = 0;
            int omitidos = 0;

            for (ReporteDto perdido : perdidos) {
                for (ReporteDto encontrado : encontrados) {
                    Long idP = perdido.getIdReporteMascota().longValue();
                    Long idE = encontrado.getIdReporteMascota().longValue();

                    if (coincidenciaRequestRepository
                            .existsByReportePerdido_IdReporteMascotaAndReporteEncontrado_IdReporteMascota(idP, idE)) {
                        omitidos++;
                        continue;
                    }

                    try {
                        var solicitud = matchingService.solicitarCoincidencia(idP, idE);
                        matchingService.procesarCoincidencia(solicitud.idCoincidenciaRequest());
                        procesados++;
                        log.debug("[MatchingJob] Par ({}, {}) procesado.", idP, idE);
                    } catch (Exception ex) {
                        log.error("[MatchingJob] Error procesando par ({}, {}): {}", idP, idE, ex.getMessage());
                    }
                }
            }

            log.info("[MatchingJob] Ciclo completado. Procesados: {}, ya existentes (omitidos): {}.", procesados, omitidos);

        } catch (Exception ex) {
            log.error("[MatchingJob] Error en ciclo de matching automático: {}", ex.getMessage());
        }
    }
}
