package org.apereo.cas.configuration.model.core.sso;

import org.apereo.cas.configuration.model.core.util.AbstractCryptographyProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for create.sso.*
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "create.sso", ignoreUnknownFields = false)
public class SsoProperties {

    private boolean renewedAuthn = true;

    private boolean missingService = true;

    public boolean isRenewedAuthn() {
        return renewedAuthn;
    }

    public void setRenewedAuthn(final boolean renewedAuthn) {
        this.renewedAuthn = renewedAuthn;
    }

    public boolean isMissingService() {
        return missingService;
    }

    public void setMissingService(final boolean missingService) {
        this.missingService = missingService;
    }
}
