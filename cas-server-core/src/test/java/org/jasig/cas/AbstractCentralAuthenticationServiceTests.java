package org.jasig.cas;

import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistrySupport;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.constraints.NotNull;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
@ContextConfiguration(locations = {
        "classpath:/core-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractCentralAuthenticationServiceTests {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TicketRegistrySupport ticketRegistrySupport;

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

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    public ArgumentExtractor getArgumentExtractor() {
        return argumentExtractor;
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
        return authenticationSystemSupport;
    }

    public TicketRegistrySupport getTicketRegistrySupport() {
        return ticketRegistrySupport;
    }
}
