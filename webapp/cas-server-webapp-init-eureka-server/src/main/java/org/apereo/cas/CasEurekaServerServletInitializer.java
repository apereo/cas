package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasSpringBootServletInitializer;

import org.springframework.core.metrics.ApplicationStartup;

import java.util.List;

/**
 * This is {@link CasEurekaServerServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasEurekaServerServletInitializer extends AbstractCasSpringBootServletInitializer {
    public CasEurekaServerServletInitializer() {
        super(List.of(CasEurekaServerWebApplication.class),
            new CasEurekaServerBanner(), ApplicationStartup.DEFAULT);
    }
}
