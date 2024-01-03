package org.apereo.cas.tokens;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasRestAutoConfiguration;
import org.apereo.cas.config.CasRestTokensConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.List;

/**
 * This is {@link BaseTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    BaseTicketResourceEntityResponseFactoryTests.TicketResourceTestConfiguration.class,
    CasCoreRestAutoConfiguration.class,
    CasRestTokensConfiguration.class,
    CasRestAutoConfiguration.class,
    TokenCoreConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCookieAutoConfiguration.class
})
public abstract class BaseTicketResourceEntityResponseFactoryTests {
    @Autowired
    @Qualifier("ticketGrantingTicketResourceEntityResponseFactory")
    protected TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    protected CipherExecutor tokenCipherExecutor;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("serviceTicketResourceEntityResponseFactory")
    protected ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory;

    @TestConfiguration(value = "TicketResourceTestConfiguration", proxyBeanMethods = false)
    static class TicketResourceTestConfiguration implements InitializingBean {

        @Autowired
        @Qualifier("inMemoryRegisteredServices")
        private List<RegisteredService> inMemoryRegisteredServices;

        public void init() {
            inMemoryRegisteredServices.add(RegisteredServiceTestUtils.getRegisteredService("https://cas.example.org.+"));
        }

        @Override
        public void afterPropertiesSet() {
            init();
        }
    }
}
