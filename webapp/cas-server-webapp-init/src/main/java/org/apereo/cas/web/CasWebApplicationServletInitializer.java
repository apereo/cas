package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;

import lombok.val;
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
        val properties = CasEmbeddedContainerUtils.getRuntimeProperties(Boolean.FALSE);
        return builder
            .sources(CasWebApplication.class)
            .properties(properties)
            .banner(CasEmbeddedContainerUtils.getCasBannerInstance());
    }
}
