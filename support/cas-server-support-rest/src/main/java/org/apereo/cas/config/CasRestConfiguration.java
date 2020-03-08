package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.audit.RestResponseEntityAuditResourceResolver;
import org.apereo.cas.rest.factory.CasProtocolServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.CompositeServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.DefaultUserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.DefaultServiceTicketResourceEntityResponseFactoryPlan;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.support.rest.resources.ServiceTicketResource;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.support.rest.resources.TicketStatusResource;
import org.apereo.cas.support.rest.resources.UserAuthenticationResource;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This is {@link CasRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casRestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasRestConfiguration {

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    public TicketStatusResource ticketStatusResource() {
        return new TicketStatusResource(centralAuthenticationService.getObject());
    }

    @Bean
    @Autowired
    public ServiceTicketResource serviceTicketResource(
        @Qualifier("serviceTicketResourceEntityResponseFactory") final ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory,
        @Qualifier("restHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory) {
        return new ServiceTicketResource(authenticationSystemSupport.getObject(),
            ticketRegistrySupport.getObject(),
            argumentExtractor.getObject(),
            serviceTicketResourceEntityResponseFactory,
            restHttpRequestCredentialFactory,
            applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceTicketResourceEntityResponseFactory")
    @Autowired
    public ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory(
        final List<ServiceTicketResourceEntityResponseFactoryConfigurer> configurers) {
        val plan = new DefaultServiceTicketResourceEntityResponseFactoryPlan();
        configurers.forEach(c -> c.configureEntityResponseFactory(plan));
        return new CompositeServiceTicketResourceEntityResponseFactory(plan.getFactories());
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketGrantingTicketResourceEntityResponseFactory")
    public TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory() {
        return new DefaultTicketGrantingTicketResourceEntityResponseFactory();
    }

    @Bean
    @ConditionalOnMissingBean(name = "userAuthenticationResourceEntityResponseFactory")
    public UserAuthenticationResourceEntityResponseFactory userAuthenticationResourceEntityResponseFactory() {
        return new DefaultUserAuthenticationResourceEntityResponseFactory();
    }

    @Autowired
    @Bean
    public TicketGrantingTicketResource ticketGrantingTicketResource(
        @Qualifier("restHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory) {
        return new TicketGrantingTicketResource(authenticationSystemSupport.getObject(),
            restHttpRequestCredentialFactory,
            centralAuthenticationService.getObject(),
            webApplicationServiceFactory.getObject(),
            ticketGrantingTicketResourceEntityResponseFactory(),
            applicationContext);
    }

    @Autowired
    @Bean
    public UserAuthenticationResource userAuthenticationRestController(
        @Qualifier("restHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory) {
        return new UserAuthenticationResource(authenticationSystemSupport.getObject(),
            restHttpRequestCredentialFactory,
            webApplicationServiceFactory.getObject(),
            userAuthenticationResourceEntityResponseFactory(),
            applicationContext);
    }

    @Autowired
    @Bean
    public RestHttpRequestCredentialFactory restHttpRequestCredentialFactory(final List<RestHttpRequestCredentialFactoryConfigurer> configurers) {
        LOGGER.trace("building REST credential factory from [{}]", configurers);

        val factory = new ChainingRestHttpRequestCredentialFactory();
        AnnotationAwareOrderComparator.sortIfNecessary(configurers);

        configurers.forEach(c -> {
            LOGGER.trace("Configuring credential factory: [{}]", c);
            c.configureCredentialFactory(factory);
        });
        return factory;
    }

    @ConditionalOnMissingBean(name = "restHttpRequestCredentialFactoryConfigurer")
    @Bean
    public RestHttpRequestCredentialFactoryConfigurer restHttpRequestCredentialFactoryConfigurer() {
        return factory -> factory.registerCredentialFactory(new UsernamePasswordRestHttpRequestCredentialFactory());
    }

    @Bean
    public ServiceTicketResourceEntityResponseFactoryConfigurer restServiceTicketResourceEntityResponseFactoryConfigurer() {
        return plan -> plan.registerFactory(new CasProtocolServiceTicketResourceEntityResponseFactory(centralAuthenticationService.getObject()));
    }

    @Bean
    @ConditionalOnMissingBean(name = "restAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer restAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditActionResolver("REST_API_TICKET_GRANTING_TICKET_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
            plan.registerAuditResourceResolver("REST_API_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER",
                new RestResponseEntityAuditResourceResolver(false));

            plan.registerAuditActionResolver("REST_API_SERVICE_TICKET_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
            plan.registerAuditResourceResolver("REST_API_SERVICE_TICKET_RESOURCE_RESOLVER",
                new RestResponseEntityAuditResourceResolver(true));
        };
    }


    /**
     * This is {@link CasRestThrottlingConfiguration}.
     *
     * @author Misagh Moayyed
     * @since 5.3.0
     */
    @Configuration("casRestThrottlingConfiguration")
    @ConditionalOnMissingBean(name = "restAuthenticationThrottle")
    @Slf4j
    public static class CasRestThrottlingConfiguration implements WebMvcConfigurer {

        @Autowired
        @Qualifier("authenticationThrottlingExecutionPlan")
        private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

        @Override
        public void addInterceptors(final InterceptorRegistry registry) {
            val plan = authenticationThrottlingExecutionPlan.getObject();
            LOGGER.debug("Activating authentication throttling for REST endpoints...");
            plan.getAuthenticationThrottleInterceptors().forEach(handler -> registry.addInterceptor(handler).addPathPatterns("/v1/**"));
        }
    }
}
