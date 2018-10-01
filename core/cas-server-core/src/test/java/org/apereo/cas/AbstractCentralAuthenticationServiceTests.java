package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@TestPropertySource(locations = {"classpath:/core.properties"})
@Setter
@Getter
public abstract class AbstractCentralAuthenticationServiceTests extends BaseCasCoreTests {
    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ServicesManager servicesManager;

    @Autowired
    private ArgumentExtractor argumentExtractor;

    @Autowired
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private WebApplicationServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @TestConfiguration
    public static class CasTestConfiguration implements InitializingBean {

        @Autowired
        protected ApplicationContext applicationContext;

        @Override
        public void afterPropertiesSet() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
