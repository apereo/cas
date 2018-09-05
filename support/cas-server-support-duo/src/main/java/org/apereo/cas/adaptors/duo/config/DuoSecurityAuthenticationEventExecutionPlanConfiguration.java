package org.apereo.cas.adaptors.duo.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.BasicDuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoDirectCredential;
import org.apereo.cas.adaptors.duo.web.flow.action.DetermineDuoUserAccountAction;
import org.apereo.cas.adaptors.duo.web.flow.action.PrepareDuoWebLoginFormAction;
import org.apereo.cas.adaptors.duo.web.flow.config.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.List;
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

    public DefaultDuoMultifactorAuthenticationProvider duoMultifactorAuthenticationProviders(final DuoSecurityMultifactorProperties duo) {
            final BasicDuoSecurityAuthenticationService s = new BasicDuoSecurityAuthenticationService(duo, httpClient);
            final DefaultDuoMultifactorAuthenticationProvider duoP =
                    new DefaultDuoMultifactorAuthenticationProvider(duo.getRegistrationUrl(), s);
            duoP.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
            duoP.setBypassEvaluator(MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(duo.getBypass()));
            duoP.setOrder(duo.getRank());
            duoP.setId(duo.getId());
            applicationContext.getBeanFactory().registerSingleton(duoP.getId()+"-provider", duoP);
            return duoP;
    }

    @Bean
    public Action prepareDuoWebLoginFormAction() {
        return new PrepareDuoWebLoginFormAction();
    }

    @Bean
    public Action determineDuoUserAccountAction() {
        return new DetermineDuoUserAccountAction();
    }

    private AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator(final DuoAuthenticationHandler duoAuthenticationHandler) {
        return new DuoAuthenticationMetaDataPopulator(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                duoAuthenticationHandler);
    }

    @RefreshScope
    @Bean
    public Collection<DuoAuthenticationHandler> duoAuthenticationHandler() {
        final List<DuoSecurityMultifactorProperties> duos = casProperties.getAuthn().getMfa().getDuo();
        if (duos.isEmpty()) {
            throw new BeanCreationException("No configuration/settings could be found for Duo Security. Review settings and ensure the correct syntax is used");
        }
        return duos.stream()
                .map(d -> new DuoAuthenticationHandler(d.getId(),
                        servicesManager,
                        duoPrincipalFactory(),
                        duoMultifactorAuthenticationProviders(d))
                ).collect(Collectors.toList());
    }

    @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer() {
        final boolean deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
        return new DuoMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry,
            deviceRegistrationEnabled,
            applicationContext,
            casProperties);
    }

    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer duoSecurityAuthenticationEventExecutionPlanConfigurer() {
        return plan -> { duoAuthenticationHandler().stream().forEach(dh -> {
                        plan.registerAuthenticationHandler(dh);
                        plan.registerMetadataPopulator(duoAuthenticationMetaDataPopulator(dh));
                });
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(DuoCredential.class, DuoDirectCredential.class));
        };
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(duoMultifactorWebflowConfigurer());
    }
}
