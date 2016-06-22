package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringApplicationConfiguration(
        classes = {CasCoreServicesConfiguration.class, 
                CasCoreUtilConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreValidationConfiguration.class},
        locations = {
                "classpath:/core-context.xml"
        }, initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class AbstractCentralAuthenticationServiceTests {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired(required = true)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = true)
    private TicketRegistry ticketRegistry;

    @Autowired(required = true)
    private AuthenticationManager authenticationManager;

    @Autowired(required = true)
    private ServicesManager servicesManager;

    @Autowired
    private ArgumentExtractor argumentExtractor;

    @Autowired
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private WebApplicationServiceFactory webApplicationServiceFactory;
    
    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    public ArgumentExtractor getArgumentExtractor() {
        return this.argumentExtractor;
    }

    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public TicketRegistry getTicketRegistry() {
        return this.ticketRegistry;
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return this.authenticationSystemSupport;
    }

    public TicketRegistrySupport getTicketRegistrySupport() {
        return this.ticketRegistrySupport;
    }

    public WebApplicationServiceFactory getWebApplicationServiceFactory() {
        return webApplicationServiceFactory;
    }
}
