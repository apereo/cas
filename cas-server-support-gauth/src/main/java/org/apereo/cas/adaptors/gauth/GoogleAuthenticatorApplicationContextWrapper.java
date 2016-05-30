package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.web.BaseApplicationContextWrapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * This is {@link GoogleAuthenticatorApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Resource(name="googleAuthenticatorAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Resource(name="googleAuthenticatorAuthenticationMetaDataPopulator")
    private AuthenticationMetaDataPopulator populator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(this.populator);
    }
}
