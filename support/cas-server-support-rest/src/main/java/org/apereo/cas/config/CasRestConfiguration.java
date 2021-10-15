package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.rest.audit.RestResponseEntityAuditResourceResolver;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.rest.factory.CasProtocolServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.CompositeServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.DefaultUserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.rest.plan.DefaultServiceTicketResourceEntityResponseFactoryPlan;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.support.rest.resources.RestProtocolConstants;
import org.apereo.cas.support.rest.resources.ServiceTicketResource;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.support.rest.resources.TicketStatusResource;
import org.apereo.cas.support.rest.resources.UserAuthenticationResource;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This is {@link CasRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "CasRestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasRestConfiguration {

    @Configuration(value = "CasRestResponseFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasRestResponseFactoryPlanConfiguration {
        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restServiceTicketResourceEntityResponseFactoryConfigurer")
        public ServiceTicketResourceEntityResponseFactoryConfigurer restServiceTicketResourceEntityResponseFactoryConfigurer(
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return plan -> plan.registerFactory(new CasProtocolServiceTicketResourceEntityResponseFactory(centralAuthenticationService));
        }

    }

    @Configuration(value = "CasRestResponseFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasRestResponseFactoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "serviceTicketResourceEntityResponseFactory")
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory(
            final List<ServiceTicketResourceEntityResponseFactoryConfigurer> configurers) {
            val plan = new DefaultServiceTicketResourceEntityResponseFactoryPlan();
            configurers.forEach(c -> c.configureEntityResponseFactory(plan));
            return new CompositeServiceTicketResourceEntityResponseFactory(plan.getFactories());
        }

        @Bean
        @ConditionalOnMissingBean(name = "ticketGrantingTicketResourceEntityResponseFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory() {
            return new DefaultTicketGrantingTicketResourceEntityResponseFactory();
        }

        @Bean
        @ConditionalOnMissingBean(name = "userAuthenticationResourceEntityResponseFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UserAuthenticationResourceEntityResponseFactory userAuthenticationResourceEntityResponseFactory() {
            return new DefaultUserAuthenticationResourceEntityResponseFactory();
        }
    }

    @Configuration(value = "CasRestWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasRestWebConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "restProtocolEndpointConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProtocolEndpointWebSecurityConfigurer<Void> restProtocolEndpointConfigurer() {
            return new ProtocolEndpointWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(RestProtocolConstants.BASE_ENDPOINT, "/"));
                }
            };
        }

    }

    @Configuration(value = "CasRestThrottleConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnBean(AuthenticationThrottlingExecutionPlan.class)
    public static class CasRestThrottleConfiguration {
        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restAuthenticationThrottle")
        public WebMvcConfigurer casRestThrottlingWebMvcConfigurer(
            @Qualifier("authenticationThrottlingExecutionPlan")
            final AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(final InterceptorRegistry registry) {
                    LOGGER.debug("Activating authentication throttling for REST endpoints...");
                    authenticationThrottlingExecutionPlan.getAuthenticationThrottleInterceptors()
                        .forEach(handler -> registry.addInterceptor(handler).order(0).addPathPatterns("/v1/**"));
                }
            };
        }
    }

    @Configuration(value = "CasRestControllerResourcesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class CasRestControllerResourcesConfiguration {
        @Bean
        @Autowired
        public TicketStatusResource ticketStatusResource(
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new TicketStatusResource(centralAuthenticationService);
        }

        @Bean
        @Autowired
        public ServiceTicketResource serviceTicketResource(
            @Qualifier("serviceTicketResourceEntityResponseFactory")
            final ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory,
            @Qualifier("restHttpRequestCredentialFactory")
            final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor) {
            return new ServiceTicketResource(authenticationSystemSupport, ticketRegistrySupport,
                argumentExtractor, serviceTicketResourceEntityResponseFactory, restHttpRequestCredentialFactory, applicationContext);
        }

        @Bean
        @Autowired
        public TicketGrantingTicketResource ticketGrantingTicketResource(
            @Qualifier("ticketGrantingTicketResourceEntityResponseFactory")
            final TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("restAuthenticationService")
            final RestAuthenticationService restAuthenticationService,
            @Qualifier("defaultSingleLogoutRequestExecutor")
            final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor) {
            return new TicketGrantingTicketResource(restAuthenticationService,
                centralAuthenticationService, ticketGrantingTicketResourceEntityResponseFactory,
                applicationContext, defaultSingleLogoutRequestExecutor);
        }

        @Bean
        @Autowired
        public UserAuthenticationResource userAuthenticationRestController(
            @Qualifier("userAuthenticationResourceEntityResponseFactory")
            final UserAuthenticationResourceEntityResponseFactory userAuthenticationResourceEntityResponseFactory,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("restAuthenticationService")
            final RestAuthenticationService restAuthenticationService) {
            return new UserAuthenticationResource(restAuthenticationService, userAuthenticationResourceEntityResponseFactory, applicationContext);
        }
    }

    @Configuration(value = "CasRestAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasRestAuditConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "restAuditTrailRecordResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer restAuditTrailRecordResolutionPlanConfigurer() {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.REST_API_TICKET_GRANTING_TICKET_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.REST_API_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER,
                    new RestResponseEntityAuditResourceResolver(false));

                plan.registerAuditActionResolver(AuditActionResolvers.REST_API_SERVICE_TICKET_ACTION_RESOLVER,
                    new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.REST_API_SERVICE_TICKET_RESOURCE_RESOLVER,
                    new RestResponseEntityAuditResourceResolver(true));
            };
        }
    }
}
