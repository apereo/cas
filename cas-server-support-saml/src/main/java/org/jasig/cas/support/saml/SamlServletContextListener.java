package org.jasig.cas.support.saml;

import org.jasig.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.jasig.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.jasig.cas.support.saml.web.SamlValidateController;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML validation endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component("samlServletContextListener")
public class SamlServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("samlServiceFactory")
    private SamlServiceFactory samlServiceFactory;

    @Autowired
    @Qualifier("samlServiceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;

    @Autowired
    @Qualifier("samlValidateController")
    private SamlValidateController samlValidateController;

    @Autowired
    @Qualifier("samlAuthenticationMetaDataPopulator")
    private SamlAuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator;

    @Override
    public void initializeServletContext(final ServletContextEvent event) {
        if (WebUtils.isCasServletInitializing(event)) {
            addEndpointMappingToCasServlet(event, SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        }
    }

    @Override
    protected void initializeRootApplicationContext() {
        addServiceFactory(samlServiceFactory);
        addServiceTicketUniqueIdGenerator(SamlService.class.getCanonicalName(),
                this.samlServiceTicketUniqueIdGenerator);
        addAuthenticationMetadataPopulator(samlAuthenticationMetaDataPopulator);
    }

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE, samlValidateController);
    }

}
