package org.apereo.cas.support.spnego;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the spnego authentication context.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("spnegoApplicationContextWrapper")
public class SpnegoApplicationContextWrapper extends BaseApplicationContextWrapper {
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
        addAuthenticationHandlerPrincipalResolver(this.spnegoHandler, this.spnegoPrincipalResolver);
        addAuthenticationMetadataPopulator(this.successfulHandlerMetaDataPopulator);
    }

}
