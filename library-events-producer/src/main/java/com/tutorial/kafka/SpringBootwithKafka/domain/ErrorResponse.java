package com.tutorial.kafka.SpringBootwithKafka.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String field;
    private String message;
}
