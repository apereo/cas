package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yubikeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YubiKeyConfiguration {

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @Autowired(required = false)
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry registry;

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

    @Bean
    public FlowDefinitionRegistry yubikeyFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-yubikey/*-webflow.xml");
        return builder.build();
    }

    @Bean
    public PrincipalFactory yubikeyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public YubiKeyAuthenticationHandler yubikeyAuthenticationHandler() {
        final MultifactorAuthenticationProperties.YubiKey yubi = this.casProperties.getAuthn().getMfa().getYubikey();

        if (StringUtils.isBlank(yubi.getSecretKey())) {
            throw new IllegalArgumentException("Yubikey secret key cannot be blank");
        }
        if (yubi.getClientId() <= 0) {
            throw new IllegalArgumentException("Yubikey client id is undefined");
        }
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(yubi.getClientId(), 
                yubi.getSecretKey(), this.registry);

        handler.setPrincipalFactory(yubikeyPrincipalFactory());
        handler.setServicesManager(servicesManager);

        if (!casProperties.getAuthn().getMfa().getYubikey().getApiUrls().isEmpty()) {
            final String[] urls = casProperties.getAuthn().getMfa().getYubikey().getApiUrls().toArray(new String[]{});
            handler.getClient().setWsapiUrls(urls);
        }
        handler.setName(yubi.getName());
        return handler;
    }

    @Bean
    @RefreshScope
    public YubiKeyAuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new YubiKeyAuthenticationMetaDataPopulator(authenticationContextAttribute, yubikeyAuthenticationHandler(), yubikeyAuthenticationProvider());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getYubikey().getBypass(), ticketRegistrySupport);
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyAuthenticationProvider() {
        final YubiKeyMultifactorAuthenticationProvider p = new YubiKeyMultifactorAuthenticationProvider(yubikeyAuthenticationHandler(), this.httpClient);
        p.setBypassEvaluator(yubikeyBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(casProperties.getAuthn().getMfa().getYubikey().getRank());
        p.setId(casProperties.getAuthn().getMfa().getYubikey().getId());
        return p;
    }

    @RefreshScope
    @Bean
    public Action yubikeyAuthenticationWebflowAction() {
        return new YubiKeyAuthenticationWebflowAction(yubikeyAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "yubikeyMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer() {
        return new YubiKeyMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, yubikeyFlowRegistry());
    }

    @Bean
    public CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver() {
        return new YubiKeyAuthenticationWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies, multifactorAuthenticationProviderSelector);
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        final MultifactorAuthenticationProperties.YubiKey yubi = casProperties.getAuthn().getMfa().getYubikey();
        if (yubi.getClientId() > 0 && StringUtils.isNotBlank(yubi.getSecretKey())) {
            this.authenticationHandlersResolvers.put(yubikeyAuthenticationHandler(), null);
            authenticationMetadataPopulators.add(0, yubikeyAuthenticationMetaDataPopulator());
        }
    }

    /**
     * The Authy multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.yubikey", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("yubiMultifactorTrustConfiguration")
    public class YubiKeyMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "yubiMultifactorTrustConfiguration")
        @Bean
        public CasWebflowConfigurer yubiMultifactorTrustConfiguration() {
            final boolean deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
            return new YubiKeyMultifactorTrustWebflowConfigurer(flowBuilderServices, deviceRegistrationEnabled, loginFlowDefinitionRegistry);
        }
    }
}
