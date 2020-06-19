package org.apereo.cas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasApplication.class, args);
    }

    @Bean
    public InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        return initializrMetadata -> initializrMetadata;
    }

}
