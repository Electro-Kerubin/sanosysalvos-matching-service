package org.sanosysalvos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.matching.exchange}")
    private String matchingExchange;

    @Value("${app.rabbitmq.matching.queue}")
    private String matchingQueue;

    @Value("${app.rabbitmq.matching.routing-key}")
    private String matchingRoutingKey;

    @Bean
    public TopicExchange matchingExchange() {
        return new TopicExchange(matchingExchange);
    }

    @Bean
    public Queue coincidenciaPotencialQueue() {
        return new Queue(matchingQueue, true);
    }

    @Bean
    public Binding coincidenciaPotencialBinding() {
        return BindingBuilder.bind(coincidenciaPotencialQueue())
                .to(matchingExchange())
                .with(matchingRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}