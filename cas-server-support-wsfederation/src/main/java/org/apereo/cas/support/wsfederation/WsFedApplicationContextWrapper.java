package org.apereo.cas.support.wsfederation;


import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * ADFS validation can be activated and authentication handlers injected.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("wsFedApplicationContextWrapper")
public class WsFedApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Autowired
    @Qualifier("adfsAuthNHandler")
    private AuthenticationHandler adfsAuthNHandler;

    @Autowired
    @Qualifier("adfsPrincipalResolver")
    private PrincipalResolver adfsPrincipalResolver;

    @Value("${cas.wsfed.idp.attribute.resolver.enabled:true}")
    private boolean useResolver;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (!this.useResolver) {
            addAuthenticationHandler(this.adfsAuthNHandler);
        } else {
            addAuthenticationHandlerPrincipalResolver(this.adfsAuthNHandler, this.adfsPrincipalResolver);
        }
    }
}

