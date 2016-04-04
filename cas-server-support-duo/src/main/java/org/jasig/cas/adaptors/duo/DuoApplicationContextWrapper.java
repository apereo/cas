package org.jasig.cas.adaptors.duo;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link DuoApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
public class DuoApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Autowired
    @Qualifier("duoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("duoAuthenticationMetaDataPopulator")
    private AuthenticationMetaDataPopulator populator;

    /**
     * Initialize servlet application context.
     */
    @PostConstruct
    protected void initializeServletApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(this.populator);
    }
}
