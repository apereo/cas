package org.apereo.cas.support.pac4j;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Initializes the pac4j support for authentication delegation.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public class Pac4jApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Resource(name="clientAuthenticationHandler")
    private ClientAuthenticationHandler clientAuthenticationHandler;

    @Resource(name="clientAuthenticationMetaDataPopulator")
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
