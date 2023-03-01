package com.learn.kafka.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learn.kafka.libraryeventsconsumer.service.LibraryEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsRetryConsumer {

    @Autowired
    private LibraryEventService libraryEventService;

    @KafkaListener(topics = {"${topics.retry}"}, groupId = "retry-library-events-group")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Retry Consumer Records : {}", consumerRecord.toString());
        libraryEventService.processLibraryEvent(consumerRecord);
        log.info("Retry Record successfully processed");
    }
}
