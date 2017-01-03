package org.apereo.cas.adaptors.duo.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.BasicDuoAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.duo.web.flow.DuoAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoDirectAuthenticationAction;
import org.apereo.cas.adaptors.duo.web.flow.action.PrepareDuoWebLoginFormAction;
import org.apereo.cas.adaptors.duo.web.flow.config.DuoMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.DefaultVariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DuoSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("duoSecurityConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DuoSecurityConfiguration.class);

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

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
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired(required = false)
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector = new FirstMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators;

    @RefreshScope
    @Bean
    public AuthenticationHandler duoAuthenticationHandler() {
        final DuoAuthenticationHandler h = new DuoAuthenticationHandler(duoMultifactorAuthenticationProvider());
        h.setPrincipalFactory(duoPrincipalFactory());
        h.setServicesManager(servicesManager);
        final String name = casProperties.getAuthn().getMfa().getDuo().stream().findFirst().get().getName();
        if (casProperties.getAuthn().getMfa().getDuo().size() > 1) {
            LOGGER.debug("Multiple Duo Security providers are available; Authentication handler is named after {}", name);
        }
        h.setName(name);

        return h;
    }

    @Bean
    public PrincipalFactory duoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator duoAuthenticationMetaDataPopulator() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new DuoAuthenticationMetaDataPopulator(authenticationContextAttribute, duoAuthenticationHandler(), duoMultifactorAuthenticationProvider());
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
                    pWeb.setBypassEvaluator(new DefaultMultifactorAuthenticationProviderBypass(duo.getBypass(), ticketRegistrySupport));
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
    public Action duoNonWebAuthenticationAction() {
        return new DuoDirectAuthenticationAction();
    }

    @Bean
    public Action duoAuthenticationWebflowAction() {
        return new DuoAuthenticationWebflowAction(duoAuthenticationWebflowEventResolver());
    }

    @Bean
    public Action prepareDuoWebLoginFormAction() {
        return new PrepareDuoWebLoginFormAction(duoMultifactorAuthenticationProvider());
    }

    @Bean
    public CasWebflowEventResolver duoAuthenticationWebflowEventResolver() {
        return new DuoAuthenticationWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies, multifactorAuthenticationProviderSelector);
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

    @PostConstruct
    protected void initializeServletApplicationContext() {
        this.authenticationHandlersResolvers.put(duoAuthenticationHandler(), null);
        authenticationMetadataPopulators.add(0, duoAuthenticationMetaDataPopulator());
    }
}
