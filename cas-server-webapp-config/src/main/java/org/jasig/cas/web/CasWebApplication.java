package org.jasig.cas.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * This is {@link CasWebApplication}.
 *
 * @author Misagh Moayyed
 * @since x.y.z
 */
@SpringBootApplication(scanBasePackages = {"org.jasig.cas", "org.pac4j.springframework.web"})
@ImportResource(locations = {"/WEB-INF/spring-configuration/*.xml", 
                             "/WEB-INF/spring-configuration/*.groovy",
                             "/WEB-INF/deployerConfigContext.xml", 
                             "classpath*:/META-INF/spring/*.xml"})
@Import(AopAutoConfiguration.class)
public final class CasWebApplication {
    private CasWebApplication() {
    }
    public static void main(final String[] args) {
        SpringApplication.run(CasWebApplication.class, args);
    }
}
