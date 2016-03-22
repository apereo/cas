package org.jasig.cas.support.saml;

import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component
public class SamlGoogleAppsApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Autowired
    @Qualifier("googleAccountsServiceFactory")
    private ServiceFactory<GoogleAccountsService> googleAccountsServiceFactory;

    /**
     * Initialize the saml googleapps context.
     */
    public SamlGoogleAppsApplicationContextWrapper() {}

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addServiceFactory(this.googleAccountsServiceFactory);
    }


}
