package org.jasig.cas.support.wsfederation;


import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * ADFS validation can be activated and authentication handlers injected.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class WsFedServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("adfsAuthNHandler")
    private AuthenticationHandler adfsAuthNHandler;

    @Autowired
    @Qualifier("adfsPrincipalResolver")
    private PrincipalResolver adfsPrincipalResolver;

    @Value("${cas.wsfed.idp.attribute.resolver.enabled:true}")
    private boolean useResolver;

    @Override
    protected void initializeRootApplicationContext() {
        if (!this.useResolver) {
            addAuthenticationHandler(adfsAuthNHandler);
        } else {
            addAuthenticationHandlerPrincipalResolver(adfsAuthNHandler, adfsPrincipalResolver);
        }
    }
}

