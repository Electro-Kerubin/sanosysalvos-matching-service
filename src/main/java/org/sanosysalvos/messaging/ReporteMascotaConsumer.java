package org.sanosysalvos.messaging;

import org.sanosysalvos.dto.ReporteMascotaEventDto;
import org.sanosysalvos.model.ReporteMascota;
import org.sanosysalvos.repository.ReporteMascotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumidor RabbitMQ del Matching Service.
 *
 * Escucha la cola "reporte-coincidencias-queue" donde el microservicio de reportes
 * publica los reportes públicos de mascotas mediante el patrón Outbox.
 *
 * Al recibir un mensaje, sincroniza (crea o actualiza) el registro en la tabla
 * local "reporte_mascota" para que el algoritmo de scoring pueda operarlo sin
 * depender de una llamada HTTP al servicio de reportes.
 */
@Component
public class ReporteMascotaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReporteMascotaConsumer.class);

    private final ReporteMascotaRepository reporteMascotaRepository;

    public ReporteMascotaConsumer(ReporteMascotaRepository reporteMascotaRepository) {
        this.reporteMascotaRepository = reporteMascotaRepository;
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

        ReporteMascota reporte = reporteMascotaRepository
                .findById(evento.getIdReporteMascota().longValue())
                .orElse(new ReporteMascota());

        reporte.setIdReporteMascota(evento.getIdReporteMascota().longValue());
        reporte.setRaza(evento.getRaza());

        // Se prioriza colorPrimario; si viene colorSecundario se concatena
        String color = evento.getColorPrimario();
        if (color != null && evento.getColorSecundario() != null && !evento.getColorSecundario().isBlank()) {
            color = color + " / " + evento.getColorSecundario();
        }
        reporte.setColor(color);

        reporte.setTamano(evento.getTamano());
        reporte.setLatitud(evento.getLatitud());
        reporte.setLongitud(evento.getLongitud());
        reporte.setFechaReporte(evento.getFechaReporte());

        reporteMascotaRepository.save(reporte);

        log.info("[RabbitMQ] ReporteMascota id={} sincronizado correctamente.", reporte.getIdReporteMascota());
    }
}

