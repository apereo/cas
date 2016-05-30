package org.apereo.cas.support.saml;

import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Resource(name="samlServiceFactory")
    private SamlServiceFactory samlServiceFactory;

    @Resource(name="samlServiceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;
    
    @Resource(name="samlAuthenticationMetaDataPopulator")
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
