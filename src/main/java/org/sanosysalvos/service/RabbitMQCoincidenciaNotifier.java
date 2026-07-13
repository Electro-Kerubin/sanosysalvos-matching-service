package org.sanosysalvos.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.sanosysalvos.client.ReportesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RabbitMQCoincidenciaNotifier implements CoincidenciaNotifier {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQCoincidenciaNotifier.class);

    private final RabbitTemplate rabbitTemplate;
    private final ReportesClient reportesClient;
    private final CoincidenciaResultRepository coincidenciaResultRepository;
    private final String matchingExchange;
    private final String matchingRoutingKey;

    public RabbitMQCoincidenciaNotifier(
            RabbitTemplate rabbitTemplate,
            ReportesClient reportesClient,
            CoincidenciaResultRepository coincidenciaResultRepository,
            @Value("${app.rabbitmq.matching.exchange}") String matchingExchange,
            @Value("${app.rabbitmq.matching.routing-key}") String matchingRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.reportesClient = reportesClient;
        this.coincidenciaResultRepository = coincidenciaResultRepository;
        this.matchingExchange = matchingExchange;
        this.matchingRoutingKey = matchingRoutingKey;
    }

    @Override
    public void notificarCoincidenciaPotencial(Long idCoincidenciaRequest, String veredictoFinal) {
        var result = coincidenciaResultRepository
                .findByCoincidenciaRequest_IdCoincidenciaRequest(idCoincidenciaRequest)
                .orElseThrow();

        Long idPerdido = result.getCoincidenciaRequest().getReportePerdido().getIdReporteMascota();
        Long idEncontrado = result.getCoincidenciaRequest().getReporteEncontrado().getIdReporteMascota();

        // El dueño que publicó la búsqueda es el del reporte de tipo "perdida"
        ReportesClient.ReporteContactoInfo info = reportesClient.obtenerInfoReporte(idPerdido);

        Map<String, Object> payload = new HashMap<>();
        payload.put("coincidenciaId", String.valueOf(idCoincidenciaRequest));
        payload.put("reporteMascotaPerdidaId", String.valueOf(idPerdido));
        payload.put("reporteMascotaEncontradaId", String.valueOf(idEncontrado));
        payload.put("porcentajeCoincidencia", result.getPuntajeTotal().doubleValue());
        payload.put("destinatarioId", info.idContacto != null ? String.valueOf(info.idContacto) : null);
        payload.put("destinatarioEmail", info.correoContacto);
        payload.put("destinatarioNombre", info.nombresContacto);
        payload.put("nombreMascota", info.nombreMascota);
        payload.put("fechaDeteccion", Instant.now().toString());

        log.info("[RabbitMQ] Publicando coincidencia id={} veredicto={} destinatario={}",
                idCoincidenciaRequest, veredictoFinal, info.correoContacto);

        rabbitTemplate.convertAndSend(matchingExchange, matchingRoutingKey, payload);
    }
}