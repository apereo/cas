package org.apereo.cas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

@SpringBootApplication
public class CasOverlayInitializrApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CasOverlayInitializrApplication.class, args);
    }
}
