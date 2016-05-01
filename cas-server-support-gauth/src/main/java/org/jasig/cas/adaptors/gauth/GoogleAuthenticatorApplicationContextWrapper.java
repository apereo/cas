package org.jasig.cas.adaptors.gauth;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link GoogleAuthenticatorApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
public class GoogleAuthenticatorApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationMetaDataPopulator")
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
