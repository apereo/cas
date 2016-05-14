package org.apereo.cas.adaptors.radius.web;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link RadiusApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("radiusApplicationContextWrapper")
public class RadiusApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Autowired
    @Qualifier("radiusAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
    }
}
