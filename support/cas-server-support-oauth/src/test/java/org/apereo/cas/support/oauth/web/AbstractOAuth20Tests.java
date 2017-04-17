package org.apereo.cas.support.oauth.web;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuthAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AbstractOAuth20Tests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                CasCoreAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasOAuth20TestAuthenticationEventExecutionPlanConfiguration.class,
                CasDefaultServiceTicketIdGeneratorsConfiguration.class,
                CasCoreTicketIdGeneratorsConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasOAuthConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreConfiguration.class,
                CasCookieConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasOAuthAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasOAuth20TestAuthenticationEventExecutionPlanConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                AbstractOAuth20Tests.OAuthTestConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreUtilConfiguration.class})
@DirtiesContext
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
public abstract class AbstractOAuth20Tests {
    @Autowired
    protected ApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    
    
    @TestConfiguration
    public static class OAuthTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;
        
        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
        
        @Bean
        public List inMemoryRegisteredServices() {
            final AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("^(https?|imaps?)://.*");
            svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
            final List l = new ArrayList();
            l.add(svc);
            return l;
        }
    }
}
