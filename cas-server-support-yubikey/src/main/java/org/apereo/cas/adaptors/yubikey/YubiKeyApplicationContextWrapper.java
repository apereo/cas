package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationHandler;

import javax.annotation.PostConstruct;

/**
 * This is {@link YubiKeyApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyApplicationContextWrapper extends BaseApplicationContextWrapper {

    private AuthenticationHandler authenticationHandler;

    private YubiKeyAuthenticationMetaDataPopulator populator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(this.populator);
    }

    public void setAuthenticationHandler(final AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    public void setPopulator(final YubiKeyAuthenticationMetaDataPopulator populator) {
        this.populator = populator;
    }
}
