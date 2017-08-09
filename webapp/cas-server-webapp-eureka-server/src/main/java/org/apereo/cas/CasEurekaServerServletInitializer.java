package org.apereo.cas;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * This is {@link CasEurekaServerServletInitializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasEurekaServerServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder
                .sources(CasEurekaServerWebApplication.class)
                .banner(new CasEurekaServerBanner())
                .logStartupInfo(true);
    }
}
