package org.sanosysalvos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el Matching Service.
 *
 * Consume desde la cola "reporte-coincidencias-queue" publicada por el
 * microservicio de reportes mediante el patrón Outbox.
 *
 * Topología (espejo de la del reporte-service):
 *   Exchange (topic): reporte-exchange
 *       └── Queue durable: reporte-coincidencias-queue
 *               binding routing-key: reporte.nuevo
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public TopicExchange reporteExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue reporteCoincidenciasQueue() {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding reporteBinding(Queue reporteCoincidenciasQueue, TopicExchange reporteExchange) {
        return BindingBuilder
                .bind(reporteCoincidenciasQueue)
                .to(reporteExchange)
                .with(routingKey);
    }

    /** Convierte automáticamente JSON ↔ objetos Java en los listeners */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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

