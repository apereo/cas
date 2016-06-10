package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.DuoApplicationContextWrapper;
import org.apereo.cas.adaptors.duo.DuoAuthenticationHandler;
import org.apereo.cas.adaptors.duo.DuoAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.duo.DuoAuthenticationService;
import org.apereo.cas.adaptors.duo.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.web.flow.DuoAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.DuoAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.Resource;

/**
 * This is {@link DuoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("duoConfiguration")
public class DuoConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Resource(name = "builder")
    private FlowBuilderServices builder;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public FlowDefinitionRegistry duoFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.builder);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-duo/*-webflow.xml");
        return builder.build();
    }

    @Bean
    public BaseApplicationContextWrapper duoApplicationContextWrapper() {
        return new DuoApplicationContextWrapper();
    }

    @Bean
    public AuthenticationHandler duoAuthenticationHandler() {
        return new DuoAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator() {
        final DuoAuthenticationMetaDataPopulator pop = new DuoAuthenticationMetaDataPopulator();

        pop.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        pop.setAuthenticationHandler(duoAuthenticationHandler());
        pop.setProvider(duoAuthenticationProvider());
        
        return pop;
    }

    @Bean
    @RefreshScope
    public DuoAuthenticationService duoAuthenticationService() {
        return new DuoAuthenticationService();
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider duoAuthenticationProvider() {
        return new DuoMultifactorAuthenticationProvider();
    }

    @Bean
    public Action duoAuthenticationWebflowAction() {
        return new DuoAuthenticationWebflowAction();
    }

    @Bean
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver() {
        return new DuoAuthenticationWebflowEventResolver();
    }

    @Bean
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer() {
        return new DuoMultifactorWebflowConfigurer();
    }

}
