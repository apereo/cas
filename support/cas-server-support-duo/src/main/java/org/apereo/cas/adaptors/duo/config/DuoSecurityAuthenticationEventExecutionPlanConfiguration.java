package org.apereo.cas.adaptors.duo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicator;
import org.apereo.cas.adaptors.duo.authn.BasicDuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoDirectCredential;
import org.apereo.cas.adaptors.duo.web.flow.action.DetermineDuoUserAccountAction;
import org.apereo.cas.adaptors.duo.web.flow.action.PrepareDuoWebLoginFormAction;
import org.apereo.cas.adaptors.duo.web.flow.config.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.DefaultVariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

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
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "duoPrincipalFactory")
    @Bean
    public PrincipalFactory duoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "duoMultifactorAuthenticationProvider")
    @Bean
    @RefreshScope
    public VariegatedMultifactorAuthenticationProvider duoMultifactorAuthenticationProvider() {
        final var provider = new DefaultVariegatedMultifactorAuthenticationProvider();

        casProperties.getAuthn().getMfa().getDuo()
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getDuoApiHost())
                && StringUtils.isNotBlank(duo.getDuoIntegrationKey())
                && StringUtils.isNotBlank(duo.getDuoSecretKey())
                && StringUtils.isNotBlank(duo.getDuoApplicationKey()))
            .forEach(duo -> {
                final var s = new BasicDuoSecurityAuthenticationService(duo, httpClient);
                final var duoP =
                    new DefaultDuoMultifactorAuthenticationProvider(duo.getRegistrationUrl(), s);
                duoP.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
                duoP.setBypassEvaluator(MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(duo.getBypass()));
                duoP.setOrder(duo.getRank());
                duoP.setId(duo.getId());
                provider.addProvider(duoP);
            });

        if (provider.getProviders().isEmpty()) {
            throw new IllegalArgumentException("At least one Duo instance must be defined");
        }
        return provider;
    }

    @Bean
    public Action prepareDuoWebLoginFormAction() {
        return new PrepareDuoWebLoginFormAction(duoMultifactorAuthenticationProvider(), applicationContext);
    }

    @Bean
    public Action determineDuoUserAccountAction() {
        return new DetermineDuoUserAccountAction(duoMultifactorAuthenticationProvider(), applicationContext);
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator() {
        final var authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(authenticationContextAttribute, duoAuthenticationHandler(),
            duoMultifactorAuthenticationProvider());
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler duoAuthenticationHandler() {
        final var duos = casProperties.getAuthn().getMfa().getDuo();
        if (duos.isEmpty()) {
            throw new BeanCreationException("No configuration/settings could be found for Duo Security. Review settings and ensure the correct syntax is used");
        }
        final var name = duos.get(0).getName();
        if (duos.size() > 1) {
            LOGGER.debug("Multiple Duo Security providers are available; Duo authentication handler is named after [{}]", name);
        }
        final var h = new DuoAuthenticationHandler(name, servicesManager, duoPrincipalFactory(), duoMultifactorAuthenticationProvider());
        return h;
    }

    @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer() {
        final var deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
        return new DuoMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry,
            deviceRegistrationEnabled,
            duoMultifactorAuthenticationProvider(),
            applicationContext,
            casProperties);
    }

    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer duoSecurityAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(duoAuthenticationHandler());
            plan.registerMetadataPopulator(duoAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(DuoCredential.class, DuoDirectCredential.class));
        };
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(duoMultifactorWebflowConfigurer());
    }

    @Bean
    public HealthIndicator duoSecurityHealthIndicator() {
        return new DuoSecurityHealthIndicator(duoMultifactorAuthenticationProvider());
    }
}
