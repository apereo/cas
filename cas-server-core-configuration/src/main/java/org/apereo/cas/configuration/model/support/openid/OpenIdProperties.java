package org.apereo.cas.configuration.model.support.openid;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link OpenIdProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class OpenIdProperties {
    private boolean enforceRpId;

    public boolean isEnforceRpId() {
        return enforceRpId;
    }

    public void setEnforceRpId(final boolean enforceRpId) {
        this.enforceRpId = enforceRpId;
    }
}

