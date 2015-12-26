package org.jasig.cas.support.saml;

import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class SamlGoogleAppsServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("googleAccountsServiceFactory")
    private ServiceFactory<GoogleAccountsService> googleAccountsServiceFactory;

    /**
     * Initialize the saml googleapps context.
     */
    public SamlGoogleAppsServletContextListener() {}

    @Override
    protected void initializeRootApplicationContext() {
        super.initializeRootApplicationContext();
        addServiceFactory(this.googleAccountsServiceFactory);
    }


}
