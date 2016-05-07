package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
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
