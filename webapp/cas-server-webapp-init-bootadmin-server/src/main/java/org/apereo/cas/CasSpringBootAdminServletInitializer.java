package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasSpringBootServletInitializer;

import org.springframework.core.metrics.ApplicationStartup;

import java.util.List;

/**
 * This is {@link CasSpringBootAdminServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasSpringBootAdminServletInitializer extends AbstractCasSpringBootServletInitializer {

    public CasSpringBootAdminServletInitializer() {
        super(List.of(CasSpringBootAdminServerWebApplication.class),
            new CasSpringBootAdminServerBanner(),
            ApplicationStartup.DEFAULT);
    }
}
