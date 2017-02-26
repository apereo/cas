package org.apereo.cas.mgmt.web;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;


/**
 * This is {@link CasManagementWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasManagementWebApplicationServletInitializer extends SpringBootServletInitializer {

    public CasManagementWebApplicationServletInitializer() {
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder.sources(CasManagementWebApplication.class).banner(new CasManagementBanner());
    }
}
