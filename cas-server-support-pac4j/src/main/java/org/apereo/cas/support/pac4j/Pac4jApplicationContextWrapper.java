package org.apereo.cas.support.pac4j;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Initializes the pac4j support for authentication delegation.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public class Pac4jApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Autowired
    @Qualifier("clientAuthenticationHandler")
    private ClientAuthenticationHandler clientAuthenticationHandler;

    @Autowired
    @Qualifier("clientAuthenticationMetaDataPopulator")
    private AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.clientAuthenticationHandler);
        addAuthenticationMetadataPopulator(this.clientAuthenticationMetaDataPopulator);
    }
}
