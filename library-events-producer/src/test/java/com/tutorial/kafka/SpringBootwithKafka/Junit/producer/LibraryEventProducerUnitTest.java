package com.tutorial.kafka.SpringBootwithKafka.Junit.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorial.kafka.SpringBootwithKafka.domain.Book;
import com.tutorial.kafka.SpringBootwithKafka.domain.LibraryEvent;
import com.tutorial.kafka.SpringBootwithKafka.producer.LibraryEventProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @InjectMocks
    LibraryEventProducer libraryEventProducer;

    @Spy
    ObjectMapper objectMapper;

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Test
    void sendLibraryEvent_async_approach2_Failure() throws JsonProcessingException {

        Book book = Book.builder()
                .bookId(123)
                .bookName("kafka with SpringBoot")
                .bookAuthor("Prathi")
                .build();

        LibraryEvent request = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture listenableFuture
                = new SettableListenableFuture<>();
        listenableFuture.setException(new RuntimeException("Exception calling kafka"));

        when(kafkaTemplate.send(isA(ProducerRecord.class)))
                .thenReturn(listenableFuture);
        Exception exception = assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEvent_async_approach2(request).get());
        assertTrue(exception.getMessage().contains("Exception calling kafka"));
    }

    @Test
    void sendLibraryEvent_async_approach2_Success() throws JsonProcessingException, ExecutionException, InterruptedException {

        Book book = Book.builder()
                .bookId(123)
                .bookName("kafka with SpringBoot")
                .bookAuthor("Prathi")
                .build();

        LibraryEvent request = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture listenableFuture = new SettableListenableFuture<>();
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>("library-events", request.getLibraryEventId(), objectMapper.writeValueAsString(request));
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1), 1, 1, 113, 1, 1);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        listenableFuture.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(listenableFuture);
        ListenableFuture<SendResult<Integer, String>> sendResultListenableFuture = libraryEventProducer.sendLibraryEvent_async_approach2(request);
        SendResult<Integer, String> result = sendResultListenableFuture.get();

        assert result.getRecordMetadata().partition() == 1;
    }
}
