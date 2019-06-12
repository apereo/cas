package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicator;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProviderFactory;
import org.apereo.cas.adaptors.duo.web.DuoSecurityPingEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityUserAccountStatusEndpoint;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDetermineUserAccountAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityPrepareWebLoginFormAction;
import org.apereo.cas.adaptors.duo.web.flow.config.DuoSecurityMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
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
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link DuoSecurityAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("duoSecurityAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class DuoSecurityAuthenticationEventExecutionPlanConfiguration implements CasWebflowExecutionPlanConfigurer {
    @Autowired
    private GenericWebApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("duoSecurityBypassEvaluator")
    private ObjectProvider<ChainingMultifactorAuthenticationProviderBypassEvaluator> duoSecurityBypassEvaluator;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @ConditionalOnMissingBean(name = "duoPrincipalFactory")
    @Bean
    public PrincipalFactory duoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    public Action prepareDuoWebLoginFormAction() {
        return new DuoSecurityPrepareWebLoginFormAction();
    }

    @ConditionalOnMissingBean(name = "determineDuoUserAccountAction")
    @Bean
    public Action determineDuoUserAccountAction() {
        return new DuoSecurityDetermineUserAccountAction();
    }

    @ConditionalOnMissingBean(name = "duoProviderFactory")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorProperties> duoProviderFactory() {
        return new DuoSecurityMultifactorAuthenticationProviderFactory(httpClient.getIfAvailable(), duoSecurityBypassEvaluator.getIfAvailable(), failureModeEvaluator.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "duoProviderBean")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorProperties> duoProviderBean() {
        return new MultifactorAuthenticationProviderBean(duoProviderFactory(),
            applicationContext.getDefaultListableBeanFactory(),
            casProperties.getAuthn().getMfa().getDuo());
    }

    @RefreshScope
    @Bean
    public Collection<AuthenticationHandler> duoAuthenticationHandler() {
        val duos = casProperties.getAuthn().getMfa().getDuo()
            .stream()
            .filter(d -> StringUtils.isNotBlank(d.getDuoApplicationKey())
                && StringUtils.isNotBlank(d.getDuoApiHost())
                && StringUtils.isNotBlank(d.getDuoIntegrationKey())
                && StringUtils.isNotBlank(d.getDuoSecretKey()))
            .collect(Collectors.toList());
        if (duos.isEmpty()) {
            throw new BeanCreationException("No configuration/settings could be found for Duo Security. Review settings and ensure the correct syntax is used");
        }
        return duos.stream()
            .map(d -> new DuoSecurityAuthenticationHandler(d.getId(),
                servicesManager.getIfAvailable(),
                duoPrincipalFactory(),
                duoProviderBean().getProvider(d.getId()),
                d.getOrder())
            ).collect(Collectors.toList());
    }

    @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer() {
        val deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
        return new DuoSecurityMultifactorWebflowConfigurer(flowBuilderServices.getIfAvailable(),
            loginFlowDefinitionRegistry.getIfAvailable(),
            deviceRegistrationEnabled,
            applicationContext,
            casProperties);
    }

    private AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator(final AuthenticationHandler authenticationHandler) {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            authenticationHandler,
            duoProviderBean().getProvider(authenticationHandler.getName()).getId()
        );
    }

    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer duoSecurityAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            duoAuthenticationHandler()
                .forEach(dh -> {
                    plan.registerAuthenticationHandler(dh);
                    plan.registerAuthenticationMetadataPopulator(duoAuthenticationMetaDataPopulator(dh));
                });
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(DuoSecurityCredential.class, DuoSecurityDirectCredential.class));
        };
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(duoMultifactorWebflowConfigurer());
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("duoSecurityHealthIndicator")
    public HealthIndicator duoSecurityHealthIndicator() {
        return new DuoSecurityHealthIndicator(applicationContext);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public DuoSecurityPingEndpoint duoPingEndpoint() {
        return new DuoSecurityPingEndpoint(casProperties, applicationContext);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public DuoSecurityUserAccountStatusEndpoint duoAccountStatusEndpoint() {
        return new DuoSecurityUserAccountStatusEndpoint(casProperties, applicationContext);
    }
}
