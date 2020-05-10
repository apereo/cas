package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

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
            .banner(CasEmbeddedContainerUtils.getCasBannerInstance());
    }
}
