package org.apereo.cas;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * This is {@link CasSpringBootAdminServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasSpringBootAdminServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder
                .sources(CasSpringBootAdminServerWebApplication.class)
                .banner(new CasSpringBootAdminServerBanner())
                .logStartupInfo(true);
    }
}
