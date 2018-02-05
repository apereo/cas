package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.CompositeServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.DefaultServiceTicketResourceEntityResponseFactoryPlan;
import org.apereo.cas.rest.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.rest.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.rest.ServiceTicketResourceEntityResponseFactoryPlan;
import org.apereo.cas.rest.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.UserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.rest.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.CasProtocolServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.DefaultUserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.support.rest.resources.ServiceTicketResource;
import org.apereo.cas.support.rest.resources.TicketGrantingTicketResource;
import org.apereo.cas.support.rest.resources.TicketStatusResource;
import org.apereo.cas.support.rest.resources.UserAuthenticationResource;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
public class CasRestConfiguration implements RestHttpRequestCredentialFactoryConfigurer, ServiceTicketResourceEntityResponseFactoryConfigurer {

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;


    @Autowired
    @Qualifier("argumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Bean
    public TicketStatusResource ticketStatusResource() {
        return new TicketStatusResource(centralAuthenticationService);
    }

    @Bean
    @Autowired
    public ServiceTicketResource serviceTicketResource(
        @Qualifier("serviceTicketResourceEntityResponseFactory") final ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory) {
        return new ServiceTicketResource(authenticationSystemSupport, ticketRegistrySupport,
            argumentExtractor, serviceTicketResourceEntityResponseFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceTicketResourceEntityResponseFactory")
    @Autowired
    public ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory(
        final List<ServiceTicketResourceEntityResponseFactoryConfigurer> configurers) {
        final DefaultServiceTicketResourceEntityResponseFactoryPlan plan = new DefaultServiceTicketResourceEntityResponseFactoryPlan();
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
    public TicketGrantingTicketResource ticketResourceRestController(
        @Qualifier("restHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory) {
        return new TicketGrantingTicketResource(authenticationSystemSupport, restHttpRequestCredentialFactory,
            centralAuthenticationService, webApplicationServiceFactory, ticketGrantingTicketResourceEntityResponseFactory());
    }

    @Autowired
    @Bean
    public UserAuthenticationResource userAuthenticationRestController(
        @Qualifier("restHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory) {
        return new UserAuthenticationResource(authenticationSystemSupport, restHttpRequestCredentialFactory,
            webApplicationServiceFactory, userAuthenticationResourceEntityResponseFactory());
    }

    @Autowired
    @Bean
    public RestHttpRequestCredentialFactory restHttpRequestCredentialFactory(final List<RestHttpRequestCredentialFactoryConfigurer> configurers) {
        final ChainingRestHttpRequestCredentialFactory factory = new ChainingRestHttpRequestCredentialFactory();
        configurers.forEach(c -> c.registerCredentialFactory(factory));
        return factory;
    }

    @Override
    public void registerCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
        factory.registerCredentialFactory(new UsernamePasswordRestHttpRequestCredentialFactory());
    }

    @Override
    public void configureEntityResponseFactory(final ServiceTicketResourceEntityResponseFactoryPlan plan) {
        plan.registerFactory(new CasProtocolServiceTicketResourceEntityResponseFactory(this.centralAuthenticationService));
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
    public static class CasRestThrottlingConfiguration extends WebMvcConfigurerAdapter {

        @Autowired(required = false)
        @Qualifier("authenticationThrottle")
        private ThrottledSubmissionHandlerInterceptor handlerInterceptor;

        @Override
        public void addInterceptors(final InterceptorRegistry registry) {
            if (handlerInterceptor != null) {
                LOGGER.debug("Activating authentication throttling for REST endpoints...");
                registry.addInterceptor(handlerInterceptor).addPathPatterns("/v1/**");
            }
        }
    }
}
