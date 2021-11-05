package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.util.spring.boot.AbstractCasSpringBootServletInitializer;

import lombok.val;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.List;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebApplicationServletInitializer extends AbstractCasSpringBootServletInitializer {

    public CasWebApplicationServletInitializer() {
        super(List.of(CasWebApplication.class),
            CasEmbeddedContainerUtils.getCasBannerInstance(),
            CasEmbeddedContainerUtils.getApplicationStartup());
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder givenBuilder) {
        val builder = super.configure(givenBuilder);
        builder.contextFactory(webApplicationType -> new CasWebApplicationContext());
        return builder;
    }
}

