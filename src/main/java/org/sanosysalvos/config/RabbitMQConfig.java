package org.sanosysalvos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el Matching Service.
 *
 * ENTRADA — consume desde el reporte-service:
 *   Exchange (topic): reporte-exchange
 *       └── Queue durable: reporte-coincidencias-queue  (routing-key: reporte.nuevo)
 *
 * SALIDA — publica para el notificaciones-service:
 *   Exchange (topic): matching-exchange
 *       └── Queue durable: coincidencia-potencial-queue (routing-key: coincidencia.potencial)
 */
@Configuration
public class RabbitMQConfig {

    // ── ENTRADA (reporte-service → matching-service) ──────────────────────────

    @Value("${app.rabbitmq.exchange}")
    private String reporteExchangeName;

    @Value("${app.rabbitmq.queue}")
    private String reporteQueue;

    @Value("${app.rabbitmq.routing-key}")
    private String reporteRoutingKey;

    // ── SALIDA (matching-service → notificaciones-service) ───────────────────

    @Value("${app.rabbitmq.matching.exchange}")
    private String matchingExchangeName;

    @Value("${app.rabbitmq.matching.queue}")
    private String matchingQueue;

    @Value("${app.rabbitmq.matching.routing-key}")
    private String matchingRoutingKey;

    // ── Beans de ENTRADA ─────────────────────────────────────────────────────

    @Bean
    public TopicExchange reporteExchange() {
        return new TopicExchange(reporteExchangeName, true, false);
    }

    @Bean
    public Queue reporteCoincidenciasQueue() {
        return QueueBuilder.durable(reporteQueue).build();
    }

    @Bean
    public Binding reporteBinding(Queue reporteCoincidenciasQueue, TopicExchange reporteExchange) {
        return BindingBuilder
                .bind(reporteCoincidenciasQueue)
                .to(reporteExchange)
                .with(reporteRoutingKey);
    }

    // ── Beans de SALIDA ──────────────────────────────────────────────────────

    @Bean
    public TopicExchange matchingExchange() {
        return new TopicExchange(matchingExchangeName, true, false);
    }

    @Bean
    public Queue coincidenciaPotencialQueue() {
        return QueueBuilder.durable(matchingQueue).build();
    }

    @Bean
    public Binding matchingBinding(Queue coincidenciaPotencialQueue, TopicExchange matchingExchange) {
        return BindingBuilder
                .bind(coincidenciaPotencialQueue)
                .to(matchingExchange)
                .with(matchingRoutingKey);
    }

    // ── Conversor JSON ────────────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
