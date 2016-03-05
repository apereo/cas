package org.jasig.cas.web;

import org.jasig.cas.web.support.CasBanner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * This is {@link CasManagementWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class CasManagementWebApplicationServletInitializer extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder.sources(CasManagementWebApplication.class).banner(new CasBanner());
    }
}
