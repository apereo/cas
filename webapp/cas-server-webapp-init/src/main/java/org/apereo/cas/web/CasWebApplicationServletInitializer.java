package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.util.spring.boot.AbstractCasSpringBootServletInitializer;

import java.util.List;

/**
 * This is {@link CasWebApplicationServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebApplicationServletInitializer extends AbstractCasSpringBootServletInitializer {

    public CasWebApplicationServletInitializer() {
        super(List.of(CasWebApplication.class), CasEmbeddedContainerUtils.getCasBannerInstance(), CasEmbeddedContainerUtils.getApplicationStartup());
    }
}

