package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.policy.any.tryAll=true",
    "spring.aop.proxy-target-class=true",
    "cas.ticket.st.timeToKillInSeconds=30"
})
@Setter
@Getter
public abstract class AbstractCentralAuthenticationServiceTests extends BaseCasCoreTests {
    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("casAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("argumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @TestConfiguration("CasTestConfiguration")
    @Lazy(false)
    public static class CasTestConfiguration implements InitializingBean {


        @Autowired
        protected ApplicationContext applicationContext;

        @Override
        public void afterPropertiesSet() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
