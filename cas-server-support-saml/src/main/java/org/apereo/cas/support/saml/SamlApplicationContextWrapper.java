package org.apereo.cas.support.saml;

import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
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
public class SamlApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Autowired
    @Qualifier("samlServiceFactory")
    private SamlServiceFactory samlServiceFactory;

    @Autowired
    @Qualifier("samlServiceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;
    
    @Autowired
    @Qualifier("samlAuthenticationMetaDataPopulator")
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
}
