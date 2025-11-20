package com.example.ex02.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;

@Configuration
public class LiftConfig {

    @Bean
    public Map<String, Map<String, Map<String, Double>>> liftJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                new ClassPathResource("tag_lift.json").getInputStream(),
                new TypeReference<>() {}
        );
    }
}
