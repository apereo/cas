package org.apereo.cas.configuration.model.support.clearpass;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link ClearpassProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.clearpass", ignoreUnknownFields = false)
public class ClearpassProperties {
    private boolean cacheCredential;

    public boolean isCacheCredential() {
        return cacheCredential;
    }

    public void setCacheCredential(final boolean cacheCredential) {
        this.cacheCredential = cacheCredential;
    }
}
