package com.example.daj2ee.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson configuration.
 *
 * - Registers JavaTimeModule so Java 8 date/time types (Instant, LocalDateTime, etc.)
 *   serialize as ISO-8601 strings instead of timestamps.
 * - Ignores unknown JSON properties on deserialization (forward-compatible API).
 * - Skips null fields in serialization output (cleaner API responses).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}
