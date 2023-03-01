package com.tutorial.kafka.SpringBootwithKafka.Junit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorial.kafka.SpringBootwithKafka.controller.LibraryEventController;
import com.tutorial.kafka.SpringBootwithKafka.domain.Book;
import com.tutorial.kafka.SpringBootwithKafka.domain.LibraryEvent;
import com.tutorial.kafka.SpringBootwithKafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)
public class LibraryEventControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Spy
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEventTest() throws Exception {
        Book book = Book.builder()
                .bookId(123)
                .bookName("kafka with SpringBoot")
                .bookAuthor("Prathi")
                .build();

        LibraryEvent request = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String req = objectMapper.writeValueAsString(request);
        when(libraryEventProducer.sendLibraryEvent_async_approach2(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc.perform(post("/v1/library-event")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
    }

    @Test
    void postLibraryEventTest_4xx() throws Exception {
        Book book = Book.builder()
                .bookId(null)
                .bookName(null)
                .bookAuthor("Prathi")
                .build();

        LibraryEvent request = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String req = objectMapper.writeValueAsString(request);
        when(libraryEventProducer.sendLibraryEvent_async_approach2(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc.perform(post("/v1/library-event")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void putLibraryEventTest() throws Exception {
        Book book = Book.builder()
                .bookId(123)
                .bookName("kafka with SpringBoot")
                .bookAuthor("Prathi")
                .build();

        LibraryEvent request = LibraryEvent.builder()
                .libraryEventId(112)
                .book(book)
                .build();

        String req = objectMapper.writeValueAsString(request);
        when(libraryEventProducer.sendLibraryEvent_async_approach2(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc.perform(put("/v1/library-event")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void putLibraryEventTest_4xx() throws Exception {
        Book book = Book.builder()
                .bookId(123)
                .bookName("kafka with SpringBoot")
                .bookAuthor("Prathi")
                .build();

        LibraryEvent request = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String req = objectMapper.writeValueAsString(request);
        when(libraryEventProducer.sendLibraryEvent_async_approach2(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc.perform(put("/v1/library-event")
                        .content(req)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }
}
