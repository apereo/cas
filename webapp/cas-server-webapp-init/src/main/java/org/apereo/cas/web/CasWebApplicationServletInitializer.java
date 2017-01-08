package org.apereo.cas.web;

import org.apereo.cas.config.CasEmbeddedContainerConfiguration;
import org.apereo.cas.util.spring.boot.CasBanner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.util.Collections;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebApplicationServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder
                .sources(CasWebApplication.class)
                .properties(Collections.singletonMap(CasEmbeddedContainerConfiguration.EMBEDDED_CONTAINER_CONFIG_ACTIVE, Boolean.FALSE))
                .banner(new CasBanner());
    }
}
