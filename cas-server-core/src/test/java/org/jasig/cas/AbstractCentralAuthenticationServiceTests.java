package org.jasig.cas;

import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationTransactionManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
@ContextConfiguration(locations = {
        "classpath:/core-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractCentralAuthenticationServiceTests {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    private AuthenticationTransactionManager authenticationTransactionManager;

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

    public AuthenticationTransactionManager getAuthenticationTransactionManager() {
        return authenticationTransactionManager;
    }

    public TicketRegistry getTicketRegistry() {
        return this.ticketRegistry;
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }
}
