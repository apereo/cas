package org.apereo.cas.support.spnego;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;

import javax.annotation.PostConstruct;

/**
 * Initializes the spnego authentication context.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SpnegoApplicationContextWrapper extends BaseApplicationContextWrapper {

    private AuthenticationHandler spnegoHandler;

    private PrincipalResolver spnegoPrincipalResolver;
    
    private AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(this.spnegoHandler, this.spnegoPrincipalResolver);
        addAuthenticationMetadataPopulator(this.successfulHandlerMetaDataPopulator);
    }

    public void setSpnegoHandler(final AuthenticationHandler spnegoHandler) {
        this.spnegoHandler = spnegoHandler;
    }

    public void setSpnegoPrincipalResolver(final PrincipalResolver spnegoPrincipalResolver) {
        this.spnegoPrincipalResolver = spnegoPrincipalResolver;
    }

    public void setSuccessfulHandlerMetaDataPopulator(final AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator) {
        this.successfulHandlerMetaDataPopulator = successfulHandlerMetaDataPopulator;
    }
}
