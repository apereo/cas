package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicator;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProviderFactory;
import org.apereo.cas.adaptors.duo.config.cond.ConditionalOnDuoSecurityAdminApiConfigured;
import org.apereo.cas.adaptors.duo.config.cond.ConditionalOnDuoSecurityConfigured;
import org.apereo.cas.adaptors.duo.web.DuoSecurityAdminApiEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityPingEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityUserAccountStatusEndpoint;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDetermineUserAccountAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityPrepareWebLoginFormAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderFactoryBean;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This is {@link DuoSecurityAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnDuoSecurityConfigured
@Configuration(value = "duoSecurityAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class DuoSecurityAuthenticationEventExecutionPlanConfiguration {

    private static AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator(final DuoSecurityAuthenticationHandler authenticationHandler,
                                                                                      final CasConfigurationProperties casProperties) {
        return new AuthenticationContextAttributeMetaDataPopulator(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
            authenticationHandler, authenticationHandler.getMultifactorAuthenticationProvider().getId());
    }

    @ConditionalOnMissingBean(name = "duoPrincipalFactory")
    @Bean
    public PrincipalFactory duoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "prepareDuoWebLoginFormAction")
    public Action prepareDuoWebLoginFormAction() {
        return new DuoSecurityPrepareWebLoginFormAction();
    }

    @ConditionalOnMissingBean(name = "determineDuoUserAccountAction")
    @Bean
    @RefreshScope
    public Action determineDuoUserAccountAction() {
        return new DuoSecurityDetermineUserAccountAction();
    }

    @ConditionalOnMissingBean(name = "duoProviderFactory")
    @Bean
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("httpClient")
        final HttpClient httpClient,
        @Qualifier("duoSecurityBypassEvaluator")
        final ChainingMultifactorAuthenticationProviderBypassEvaluator duoSecurityBypassEvaluator,
        @Qualifier("failureModeEvaluator")
        final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
        val resolvers = ApplicationContextProvider.getMultifactorAuthenticationPrincipalResolvers();
        return new DuoSecurityMultifactorAuthenticationProviderFactory(httpClient, duoSecurityBypassEvaluator, failureModeEvaluator, casProperties, resolvers);
    }

    @ConditionalOnMissingBean(name = "duoProviderBean")
    @Bean
    @RefreshScope
    @Autowired
    public MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean(
        final CasConfigurationProperties casProperties,
        final GenericWebApplicationContext applicationContext,
        @Qualifier("duoProviderFactory")
        final MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderFactory) {
        return new MultifactorAuthenticationProviderBean(duoProviderFactory, applicationContext.getDefaultListableBeanFactory(), casProperties.getAuthn().getMfa().getDuo());
    }

    @RefreshScope
    @Bean
    @Autowired
    public Collection<DuoSecurityAuthenticationHandler> duoAuthenticationHandlers(
        final CasConfigurationProperties casProperties,
        @Qualifier("duoPrincipalFactory")
        final PrincipalFactory duoPrincipalFactory,
        @Qualifier("duoProviderBean")
        final MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val resolvers = ApplicationContextProvider.getMultifactorAuthenticationPrincipalResolvers();
        return casProperties.getAuthn()
            .getMfa()
            .getDuo()
            .stream()
            .map(props -> new DuoSecurityAuthenticationHandler(props.getName(),
                servicesManager, duoPrincipalFactory, duoProviderBean.getProvider(props.getId()), props.getOrder(), resolvers))
            .sorted(Comparator.comparing(DuoSecurityAuthenticationHandler::getOrder))
            .collect(Collectors.toList());
    }

    @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer(final CasConfigurationProperties casProperties,
                                                                final ConfigurableApplicationContext applicationContext,
                                                                @Qualifier("loginFlowRegistry")
                                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                @Qualifier("flowBuilderServices")
                                                                final FlowBuilderServices flowBuilderServices) {
        return new DuoSecurityMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
    }

    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer duoSecurityAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("duoAuthenticationHandlers")
        final Collection<DuoSecurityAuthenticationHandler> duoAuthenticationHandlers) {
        return plan -> {
            duoAuthenticationHandlers.forEach(dh -> {
                plan.registerAuthenticationHandler(dh);
                plan.registerAuthenticationMetadataPopulator(duoAuthenticationMetaDataPopulator(dh, casProperties));
            });
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(DuoSecurityCredential.class, DuoSecurityDirectCredential.class));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityCasWebflowExecutionPlanConfigurer")
    @Autowired
    public CasWebflowExecutionPlanConfigurer duoSecurityCasWebflowExecutionPlanConfigurer(
        @Qualifier("duoMultifactorWebflowConfigurer")
        final CasWebflowConfigurer duoMultifactorWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(duoMultifactorWebflowConfigurer);
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("duoSecurityHealthIndicator")
    @Autowired
    public HealthIndicator duoSecurityHealthIndicator(final ConfigurableApplicationContext applicationContext) {
        return new DuoSecurityHealthIndicator(applicationContext);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public DuoSecurityPingEndpoint duoPingEndpoint(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext) {
        return new DuoSecurityPingEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public DuoSecurityUserAccountStatusEndpoint duoAccountStatusEndpoint(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext) {
        return new DuoSecurityUserAccountStatusEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @ConditionalOnDuoSecurityAdminApiConfigured
    @Autowired
    public DuoSecurityAdminApiEndpoint duoAdminApiEndpoint(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext) {
        return new DuoSecurityAdminApiEndpoint(casProperties, applicationContext);
    }
}
