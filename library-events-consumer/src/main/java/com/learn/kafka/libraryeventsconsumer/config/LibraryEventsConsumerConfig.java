package com.learn.kafka.libraryeventsconsumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Value("${topics.retry}")
    String retryTopic;

    @Value("${topics.dlt}")
    String deadLetterTopic;

    public DeadLetterPublishingRecoverer publishingRecoverer() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, e) -> {
                    if (e instanceof RecoverableDataAccessException) {
                        return new TopicPartition(retryTopic, consumerRecord.partition());
                    } else {
                        return new TopicPartition(deadLetterTopic, consumerRecord.partition());
                    }
                });
        return recoverer;
    }

    public DefaultErrorHandler errorHandler() {

        var notRetryableExceptions = List.of(IllegalArgumentException.class);

        var fixedBackOff = new FixedBackOff(1000L, 2);

        var exponentialBackOff = new ExponentialBackOff();
        exponentialBackOff.setInitialInterval(1000L);
        exponentialBackOff.setMultiplier(2);
        exponentialBackOff.setMaxInterval(2000L);

        var defaultErrorHandler = new DefaultErrorHandler(publishingRecoverer(),
                fixedBackOff
//                exponentialBackOff
        );

//        notRetryableExceptions.forEach(errorHandler()::addNotRetryableExceptions);

        defaultErrorHandler.setRetryListeners((consumerRecord, e, i) -> log.info("Failed record in retry listener, exception is :{}, delivery attempts : {}", e.getMessage(), i));
        return defaultErrorHandler;
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(() -> new DefaultKafkaConsumerFactory<>(this.kafkaProperties.buildConsumerProperties())));
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

}
