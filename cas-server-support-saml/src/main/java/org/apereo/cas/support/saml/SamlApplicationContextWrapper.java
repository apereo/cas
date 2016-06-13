package org.apereo.cas.support.saml;

import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlApplicationContextWrapper extends BaseApplicationContextWrapper {
    
    private SamlServiceFactory samlServiceFactory;
    
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;
    
    private SamlAuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addServiceFactory(this.samlServiceFactory);
        addServiceTicketUniqueIdGenerator(SamlService.class.getCanonicalName(),
                this.samlServiceTicketUniqueIdGenerator);
        addAuthenticationMetadataPopulator(this.samlAuthenticationMetaDataPopulator);
    }

    public void setSamlServiceFactory(final SamlServiceFactory samlServiceFactory) {
        this.samlServiceFactory = samlServiceFactory;
    }

    public void setSamlServiceTicketUniqueIdGenerator(final UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator) {
        this.samlServiceTicketUniqueIdGenerator = samlServiceTicketUniqueIdGenerator;
    }

    public void setSamlAuthenticationMetaDataPopulator(final SamlAuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator) {
        this.samlAuthenticationMetaDataPopulator = samlAuthenticationMetaDataPopulator;
    }
}
