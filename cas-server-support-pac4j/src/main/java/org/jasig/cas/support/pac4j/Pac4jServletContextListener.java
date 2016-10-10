package org.jasig.cas.support.pac4j;

import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.jasig.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * Initializes the pac4j support for authentication delegation.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@WebListener
@Component("pac4jServletContextListener")
public class Pac4jServletContextListener extends AbstractServletContextInitializer {

    @Autowired(required = false)
    @Qualifier("clientPrincipalResolver")
    private PrincipalResolver clientPrincipalResolver;

    @Autowired
    @Qualifier("clientAuthenticationHandler")
    private ClientAuthenticationHandler clientAuthenticationHandler;

    @Autowired
    @Qualifier("clientAuthenticationMetaDataPopulator")
    private ClientAuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(clientAuthenticationHandler, clientPrincipalResolver);
        addAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
    }
}
