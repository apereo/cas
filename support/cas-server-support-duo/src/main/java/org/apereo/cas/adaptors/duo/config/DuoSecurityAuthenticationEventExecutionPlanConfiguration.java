package org.apereo.cas.adaptors.duo.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.authn.BasicDuoAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationHandler;
import org.apereo.cas.adaptors.duo.web.flow.action.PrepareDuoWebLoginFormAction;
import org.apereo.cas.adaptors.duo.web.flow.config.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.DefaultVariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link DuoSecurityAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("duoSecurityAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DuoSecurityAuthenticationEventExecutionPlanConfiguration.class);

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "duoPrincipalFactory")
    @Bean
    public PrincipalFactory duoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public VariegatedMultifactorAuthenticationProvider duoMultifactorAuthenticationProvider() {
        final DefaultVariegatedMultifactorAuthenticationProvider provider = new DefaultVariegatedMultifactorAuthenticationProvider();

        casProperties.getAuthn().getMfa().getDuo()
                .stream()
                .filter(duo -> StringUtils.isNotBlank(duo.getDuoApiHost())
                        && StringUtils.isNotBlank(duo.getDuoIntegrationKey())
                        && StringUtils.isNotBlank(duo.getDuoSecretKey())
                        && StringUtils.isNotBlank(duo.getDuoApplicationKey()))
                .forEach(duo -> {
                    final BasicDuoAuthenticationService s = new BasicDuoAuthenticationService(duo, httpClient);
                    final DefaultDuoMultifactorAuthenticationProvider pWeb = new DefaultDuoMultifactorAuthenticationProvider(s);
                    pWeb.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
                    pWeb.setBypassEvaluator(new DefaultMultifactorAuthenticationProviderBypass(duo.getBypass()));
                    pWeb.setOrder(duo.getRank());
                    pWeb.setId(duo.getId());

                    provider.addProvider(pWeb);
                });

        if (provider.getProviders().isEmpty()) {
            throw new IllegalArgumentException("At least one Duo instance must be defined");
        }
        return provider;
    }

    @Bean
    public Action prepareDuoWebLoginFormAction() {
        return new PrepareDuoWebLoginFormAction(duoMultifactorAuthenticationProvider());
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(authenticationContextAttribute, duoAuthenticationHandler(),
                duoMultifactorAuthenticationProvider());
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler duoAuthenticationHandler() {
        final DuoAuthenticationHandler h;
        final List<MultifactorAuthenticationProperties.Duo> duos = casProperties.getAuthn().getMfa().getDuo();
        if (!duos.isEmpty()) {
            final String name = duos.get(0).getName();
            if (duos.size() > 1) {
                LOGGER.debug("Multiple Duo Security providers are available; Duo authentication handler is named after [{}]", name);
            }
            h = new DuoAuthenticationHandler(name, servicesManager, duoPrincipalFactory(), duoMultifactorAuthenticationProvider());
        } else {
            h = new DuoAuthenticationHandler(StringUtils.EMPTY, servicesManager, duoPrincipalFactory(), duoMultifactorAuthenticationProvider());
            throw new BeanCreationException("No configuration/settings could be found for Duo Security. Review settings and ensure the correct syntax is used");
        }
        return h;
    }

    @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public CasWebflowConfigurer duoMultifactorWebflowConfigurer() {
        final boolean deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
        return new DuoMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, deviceRegistrationEnabled,
                duoMultifactorAuthenticationProvider());
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(duoAuthenticationHandler());
        plan.registerMetadataPopulator(duoAuthenticationMetaDataPopulator());
    }
}
