package org.apereo.cas.adaptors.radius.web;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.web.BaseApplicationContextWrapper;

import javax.annotation.PostConstruct;

/**
 * This is {@link RadiusApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusApplicationContextWrapper extends BaseApplicationContextWrapper {
    private AuthenticationHandler authenticationHandler;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
    }

    public void setAuthenticationHandler(final AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }
}
