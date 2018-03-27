package org.apereo.cas.web;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasEmbeddedContainerUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.util.Map;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class CasWebApplicationServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        final Map<String, Object> properties = CasEmbeddedContainerUtils.getRuntimeProperties(Boolean.FALSE);
        return builder
                .sources(CasWebApplication.class)
                .properties(properties)
                .banner(CasEmbeddedContainerUtils.getCasBannerInstance());
    }
}
