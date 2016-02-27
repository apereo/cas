package org.jasig.cas.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * This is {@link CasWebApplication}.
 *
 * @author Misagh Moayyed
 * @since x.y.z
 */
@SpringBootApplication(scanBasePackages = {"org.jasig.cas"})
@ImportResource(locations = {"/WEB-INF/spring-configuration/*.xml", 
                             "/WEB-INF/spring-configuration/*.groovy",
                             "/WEB-INF/deployerConfigContext.xml", 
                             "classpath*:/META-INF/spring/*.xml"})
@AopAutoConfiguration
public final class CasWebApplication {
    private CasWebApplication() {
    }
    public static void main(final String[] args) {
        SpringApplication.run(CasWebApplication.class, args);
    }
}
