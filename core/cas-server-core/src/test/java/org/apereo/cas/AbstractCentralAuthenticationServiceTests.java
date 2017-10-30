package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(
        classes = {
                AbstractCentralAuthenticationServiceTests.CasTestConfiguration.class,
                CasAuthenticationEventExecutionPlanTestConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasDefaultServiceTicketIdGeneratorsConfiguration.class,
                CasCoreTicketIdGeneratorsConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreAuthenticationConfiguration.class, CasCoreServicesAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreConfiguration.class,
                CasRegisteredServicesTestConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasCookieConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreLogoutConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreAuthenticationConfiguration.class, CasCoreServicesAuthenticationConfiguration.class,
                AopAutoConfiguration.class,
                CasPersonDirectoryTestConfiguration.class,
                CasCoreWebflowConfiguration.class,
                CasCoreValidationConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@RunWith(SpringRunner.class)
@EnableAspectJAutoProxy
@DirtiesContext
@TestPropertySource(locations = {"classpath:/core.properties"})
public abstract class AbstractCentralAuthenticationServiceTests {

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

    @TestConfiguration
    public static class CasTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }

}
