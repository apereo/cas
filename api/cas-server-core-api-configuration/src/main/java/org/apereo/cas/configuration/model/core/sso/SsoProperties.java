package org.apereo.cas.configuration.model.core.sso;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * Configuration properties class for {@code create.sso}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
public class SsoProperties implements Serializable {

    private static final long serialVersionUID = -8777647966370741733L;
    /**
     * Flag that indicates whether to create SSO session on re-newed authentication event.
     */
    private boolean renewedAuthn = true;

    /**
     * Flag that indicates whether to allow SSO session with a missing target service.
     */
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
