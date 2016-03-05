package org.jasig.cas.support.spnego;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.web.AbstractServletContextListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the spnego authentication context.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component
public class SpnegoServletContextListener extends AbstractServletContextListener {
    @Autowired
    @Qualifier("spnegoHandler")
    private AuthenticationHandler spnegoHandler;

    @Autowired
    @Qualifier("spnegoPrincipalResolver")
    private PrincipalResolver spnegoPrincipalResolver;

    @Autowired
    @Qualifier("successfulHandlerMetaDataPopulator")
    private AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(spnegoHandler, spnegoPrincipalResolver);
        addAuthenticationMetadataPopulator(successfulHandlerMetaDataPopulator);
    }

}
