package org.sanosysalvos.service;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Implementación de CoincidenciaNotifier que publica la coincidencia potencial
 * al exchange de RabbitMQ del matching-service para que el microservicio de
 * notificaciones pueda consumirla.
 *
 * Exchange: matching-exchange
 * Queue:    coincidencia-potencial-queue
 * Routing:  coincidencia.potencial
 */
@Primary
@Service
public class RabbitMQCoincidenciaNotifier implements CoincidenciaNotifier {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQCoincidenciaNotifier.class);

    private final RabbitTemplate rabbitTemplate;
    private final String matchingExchange;
    private final String matchingRoutingKey;

    public RabbitMQCoincidenciaNotifier(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.matching.exchange}") String matchingExchange,
            @Value("${app.rabbitmq.matching.routing-key}") String matchingRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.matchingExchange = matchingExchange;
        this.matchingRoutingKey = matchingRoutingKey;
    }

    @Override
    public void notificarCoincidenciaPotencial(Long idCoincidenciaRequest, String veredictoFinal) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("idCoincidenciaRequest", idCoincidenciaRequest);
        payload.put("veredictoFinal", veredictoFinal);

        log.info("[RabbitMQ] Publicando coincidencia potencial id={} veredicto={} en exchange={}",
                idCoincidenciaRequest, veredictoFinal, matchingExchange);

        rabbitTemplate.convertAndSend(matchingExchange, matchingRoutingKey, payload);

        log.info("[RabbitMQ] Coincidencia potencial id={} publicada correctamente.", idCoincidenciaRequest);
    }
}

