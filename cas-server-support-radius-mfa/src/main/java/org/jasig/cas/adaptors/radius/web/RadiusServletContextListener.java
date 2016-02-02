package org.jasig.cas.adaptors.radius.web;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * This is {@link RadiusServletContextListener}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@WebListener
@Component
public class RadiusServletContextListener extends AbstractServletContextInitializer {
    @Autowired
    @Qualifier("radiusAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
    }
}
