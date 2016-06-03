package org.apereo.cas.configuration.model.support.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for tgc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "tgc", ignoreUnknownFields = false)
public class TicketGrantingCookieProperties extends AbstractCookieProperties {

    public TicketGrantingCookieProperties() {
        super.setName("TGC");
    }

    private int rememberMeMaxAge = 1209600;

    public int getRememberMeMaxAge() {
        return rememberMeMaxAge;
    }

    public void setRememberMeMaxAge(final int rememberMeMaxAge) {
        this.rememberMeMaxAge = rememberMeMaxAge;
    }
}
