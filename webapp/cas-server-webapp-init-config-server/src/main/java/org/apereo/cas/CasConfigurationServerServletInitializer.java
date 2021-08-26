package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasSpringBootServletInitializer;

import org.springframework.core.metrics.ApplicationStartup;

import java.util.List;

/**
 * This is {@link CasConfigurationServerServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationServerServletInitializer extends AbstractCasSpringBootServletInitializer {

    public CasConfigurationServerServletInitializer() {
        super(List.of(CasConfigurationServerWebApplication.class),
            new CasConfigurationServerBanner(), ApplicationStartup.DEFAULT);
    }
}
