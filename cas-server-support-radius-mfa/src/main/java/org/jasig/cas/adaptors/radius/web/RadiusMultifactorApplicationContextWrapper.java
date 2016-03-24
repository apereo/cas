package org.jasig.cas.adaptors.radius.web;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link RadiusMultifactorApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
public class RadiusMultifactorApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Autowired
    @Qualifier("radiusTokenAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
    }
}
