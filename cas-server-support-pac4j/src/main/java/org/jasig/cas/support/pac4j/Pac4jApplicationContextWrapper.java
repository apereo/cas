package org.jasig.cas.support.pac4j;

import org.jasig.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.jasig.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the pac4j support for authentication delegation.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Component
public class Pac4jApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Autowired
    @Qualifier("clientAuthenticationHandler")
    private ClientAuthenticationHandler clientAuthenticationHandler;

    @Autowired
    @Qualifier("clientAuthenticationMetaDataPopulator")
    private ClientAuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(clientAuthenticationHandler);
        addAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
    }
}
