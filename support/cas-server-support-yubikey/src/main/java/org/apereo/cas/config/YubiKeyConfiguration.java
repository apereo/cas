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
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
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
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List authenticationMetadataPopulators;

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
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector =
            new FirstMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Bean
    public FlowDefinitionRegistry yubikeyFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder =
                new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
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

        if (StringUtils.isBlank(this.casProperties.getAuthn().getMfa().getYubikey().getSecretKey())) {
            throw new IllegalArgumentException("Yubikey secret key cannot be blank");
        }
        if (this.casProperties.getAuthn().getMfa().getYubikey().getClientId() <= 0) {
            throw new IllegalArgumentException("Yubikey client id is undefined");
        }
        final YubiKeyAuthenticationHandler handler = new YubiKeyAuthenticationHandler(
                this.casProperties.getAuthn().getMfa().getYubikey().getClientId(),
                this.casProperties.getAuthn().getMfa().getYubikey().getSecretKey());

        if (registry != null) {
            handler.setRegistry(this.registry);
        }

        handler.setPrincipalFactory(yubikeyPrincipalFactory());
        handler.setServicesManager(servicesManager);

        if (!casProperties.getAuthn().getMfa().getYubikey().getApiUrls().isEmpty()) {
            final String[] urls = casProperties.getAuthn().getMfa().getYubikey().getApiUrls().toArray(new String[]{});
            handler.getClient().setWsapiUrls(urls);
        }
        return handler;
    }

    @Bean
    @RefreshScope
    public YubiKeyAuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator() {
        final YubiKeyAuthenticationMetaDataPopulator pop = new YubiKeyAuthenticationMetaDataPopulator();

        pop.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        pop.setAuthenticationHandler(yubikeyAuthenticationHandler());
        pop.setProvider(yubikeyAuthenticationProvider());
        return pop;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getYubikey().getBypass()
        );
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyAuthenticationProvider() {
        final YubiKeyMultifactorAuthenticationProvider p = new YubiKeyMultifactorAuthenticationProvider(
                yubikeyAuthenticationHandler(),
                this.httpClient);
        p.setBypassEvaluator(yubikeyBypassEvaluator());
        return p;
    }

    @RefreshScope
    @Bean
    public Action yubikeyAuthenticationWebflowAction() {
        final YubiKeyAuthenticationWebflowAction a = new YubiKeyAuthenticationWebflowAction();
        a.setYubikeyAuthenticationWebflowEventResolver(yubikeyAuthenticationWebflowEventResolver());
        return a;
    }

    @ConditionalOnMissingBean(name = "yubikeyMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer() {
        final YubiKeyMultifactorWebflowConfigurer r = new YubiKeyMultifactorWebflowConfigurer();
        r.setYubikeyFlowRegistry(yubikeyFlowRegistry());
        r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        r.setFlowBuilderServices(flowBuilderServices);
        return r;
    }

    @Bean
    public CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver() {
        final YubiKeyAuthenticationWebflowEventResolver r = new YubiKeyAuthenticationWebflowEventResolver();
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
        return r;
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
            final YubiKeyMultifactorTrustWebflowConfigurer r = new YubiKeyMultifactorTrustWebflowConfigurer();
            r.setFlowDefinitionRegistry(yubikeyFlowRegistry());
            r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
            r.setFlowBuilderServices(flowBuilderServices);
            r.setEnableDeviceRegistration(casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled());
            return r;
        }
    }
}
