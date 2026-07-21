package cn.czu.claimpaws.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory notificationRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter rabbitMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1_000, 2.0, 10_000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }

    @Bean
    public TopicExchange domainEventsExchange() {
        return new TopicExchange("claimpaws.domain.events");
    }

    @Bean
    public Queue reservationEventsQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", "notification.reservation.events.dlq");
        return new Queue("notification.reservation.events", true, false, false, args);
    }

    @Bean
    public Queue reservationEventsDeadLetterQueue() {
        return new Queue("notification.reservation.events.dlq");
    }

    @Bean
    public Binding reservationEventsBinding() {
        return BindingBuilder.bind(reservationEventsQueue())
                .to(domainEventsExchange())
                .with("reservation.*");
    }
}
