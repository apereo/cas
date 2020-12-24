package org.apereo.cas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@code @ProjectGenerationConfiguration}-annotated types should not be
 * processed by the main ApplicationContext so make sure regular
 * classpath scanning is not enabled for packages
 * where such configuration classes reside.
 */
@SpringBootApplication(scanBasePackages = "org.apereo.cas.initializr")
public class CasInitializrApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CasInitializrApplication.class, args);
    }
}
