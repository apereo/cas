package org.apereo.cas.support.pac4j;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;

import javax.annotation.PostConstruct;

/**
 * Initializes the pac4j support for authentication delegation.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public class Pac4jApplicationContextWrapper extends BaseApplicationContextWrapper {

    private ClientAuthenticationHandler clientAuthenticationHandler;

    private AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.clientAuthenticationHandler);
        addAuthenticationMetadataPopulator(this.clientAuthenticationMetaDataPopulator);
    }

    public void setClientAuthenticationHandler(final ClientAuthenticationHandler clientAuthenticationHandler) {
        this.clientAuthenticationHandler = clientAuthenticationHandler;
    }

    public void setClientAuthenticationMetaDataPopulator(final AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator) {
        this.clientAuthenticationMetaDataPopulator = clientAuthenticationMetaDataPopulator;
    }
}
