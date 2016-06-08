package org.apereo.cas.support.wsfederation;


import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.BaseApplicationContextWrapper;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * ADFS validation can be activated and authentication handlers injected.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WsFedApplicationContextWrapper extends BaseApplicationContextWrapper {

    private AuthenticationHandler adfsAuthNHandler;
    private PrincipalResolver adfsPrincipalResolver;
    private boolean useResolver;

    public WsFedApplicationContextWrapper(final AuthenticationHandler adfsAuthNHandler,
                                          final PrincipalResolver adfsPrincipalResolver,
                                          final boolean useResolver) {
        this.adfsAuthNHandler = adfsAuthNHandler;
        this.adfsPrincipalResolver = adfsPrincipalResolver;
        this.useResolver = useResolver;
    }

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

