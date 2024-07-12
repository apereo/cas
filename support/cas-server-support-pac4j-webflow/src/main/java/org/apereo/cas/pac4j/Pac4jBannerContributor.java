package org.apereo.cas.pac4j;

import org.apereo.cas.util.spring.boot.BannerContributor;
import org.pac4j.core.client.BaseClient;
import org.springframework.core.env.Environment;
import java.util.Formatter;

/**
 * This is {@link Pac4jBannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class Pac4jBannerContributor implements BannerContributor {
    @Override
    public void contribute(final Formatter formatter, final Environment environment) {
        formatter.format("Pac4j Version: %s%n", BaseClient.class.getPackage().getImplementationVersion());
    }
}
