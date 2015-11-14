package org.jasig.cas.support.spnego;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * Initializes the spnego authentication context.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class SpnegoServletContextListener extends AbstractServletContextInitializer {
    @Autowired
    @Qualifier("spnegoHandler")
    private AuthenticationHandler spnegoHandler;

    @Autowired
    @Qualifier("spnegoPrincipalResolver")
    private PrincipalResolver spnegoPrincipalResolver;

    @Autowired
    @Qualifier("successfulHandlerMetaDataPopulator")
    private AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(spnegoHandler, spnegoPrincipalResolver);
        addAuthenticationMetadataPopulator(successfulHandlerMetaDataPopulator);
    }

}
