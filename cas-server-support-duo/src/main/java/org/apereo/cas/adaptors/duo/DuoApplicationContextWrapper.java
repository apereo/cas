package org.apereo.cas.adaptors.duo;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * This is {@link DuoApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Resource(name="duoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Resource(name="duoAuthenticationMetaDataPopulator")
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
