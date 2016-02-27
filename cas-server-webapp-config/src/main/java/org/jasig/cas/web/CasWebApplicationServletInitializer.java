package org.jasig.cas.web;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class CasWebApplicationServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder.sources(CasWebApplication.class);
    }
}
