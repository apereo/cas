package org.apereo.cas.rest.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.rest.authentication.DefaultRestAuthenticationService;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;

/**
 * This is {@link CasCoreRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "CasCoreRestConfiguration", proxyBeanMethods = true)
@Slf4j
public class CasCoreRestConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("requestedContextValidator")
    private ObjectProvider<RequestedAuthenticationContextValidator> requestedContextValidator;
    
    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private ObjectProvider<MultifactorAuthenticationTriggerSelectionStrategy> multifactorTriggerSelectionStrategy;
    
    @Bean
    @ConditionalOnMissingBean(name = "restAuthenticationService")
    public RestAuthenticationService restAuthenticationService() {
        return new DefaultRestAuthenticationService(
            authenticationSystemSupport.getObject(),
            restHttpRequestCredentialFactory(),
            webApplicationServiceFactory.getObject(),
            multifactorTriggerSelectionStrategy.getObject(),
            servicesManager.getObject(),
            requestedContextValidator.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "restHttpRequestCredentialFactory")
    public RestHttpRequestCredentialFactory restHttpRequestCredentialFactory() {
        val configurers = new ArrayList<>(applicationContext.getBeansOfType(RestHttpRequestCredentialFactoryConfigurer.class).values());
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
}
