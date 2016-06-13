package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.web.BaseApplicationContextWrapper;

import javax.annotation.PostConstruct;

/**
 * This is {@link GoogleAuthenticatorApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorApplicationContextWrapper extends BaseApplicationContextWrapper {

    private AuthenticationHandler authenticationHandler;

    private AuthenticationMetaDataPopulator populator;

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

    public void setPopulator(final AuthenticationMetaDataPopulator populator) {
        this.populator = populator;
    }
}
