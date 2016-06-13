package org.apereo.cas.adaptors.duo;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;

import javax.annotation.PostConstruct;

/**
 * This is {@link DuoApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoApplicationContextWrapper extends BaseApplicationContextWrapper {

    private AuthenticationHandler authenticationHandler;
    
    private AuthenticationMetaDataPopulator populator;

    /**
     * Initialize servlet application context.
     */
    @PostConstruct
    protected void initializeServletApplicationContext() {
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
