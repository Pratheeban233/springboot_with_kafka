package com.learn.kafka.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.libraryeventsconsumer.entity.LibraryEvent;
import com.learn.kafka.libraryeventsconsumer.repo.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryEventService {

    @Autowired
    private LibraryEventRepository libraryEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        if(libraryEvent.getLibraryEventId() !=null && libraryEvent.getLibraryEventId() ==999){
            throw new RecoverableDataAccessException("Temporary network down");
        }
        switch (libraryEvent.getLibraryEventType()){
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validateLibraryEvent(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.info("Invalid library event type");
        }
    }

    private void validateLibraryEvent(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("libraryEventId is missing");
        }
        if (libraryEventRepository.findById(libraryEvent.getLibraryEventId()).isEmpty()) {
            throw new RuntimeException("No data is present with this library eventId");
        }
        log.info("validate successful for library event");
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        log.info("library event : {}", libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Persisted the library event in the DB");
    }
}
