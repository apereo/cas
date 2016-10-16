package org.apereo.cas.adaptors.authy.config;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.authy.AuthyAuthenticationHandler;
import org.apereo.cas.adaptors.authy.AuthyAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.AuthyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowAction;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
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
 * This is {@link AuthyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("authyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthyConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;


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

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List authenticationMetadataPopulators;

    @Bean
    public FlowDefinitionRegistry authyAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-authy/*-webflow.xml");
        return builder.build();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler authyAuthenticationHandler() {
        try {
            final AuthyAuthenticationHandler h = new AuthyAuthenticationHandler(authyClientInstance());
            h.setServicesManager(servicesManager);
            h.setPrincipalFactory(authyPrincipalFactory());
            h.setForceVerification(casProperties.getAuthn().getMfa().getAuthy().isForceVerification());

            return h;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @RefreshScope
    @Bean
    public PrincipalFactory authyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator() {
        final AuthyAuthenticationMetaDataPopulator g =
                new AuthyAuthenticationMetaDataPopulator(
                        casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                        authyAuthenticationHandler(),
                        authyAuthenticatorAuthenticationProvider()
                );
        return g;
    }

    @Bean
    @RefreshScope
    public AbstractMultifactorAuthenticationProvider authyAuthenticatorAuthenticationProvider() {
        final AuthyMultifactorAuthenticationProvider p = new AuthyMultifactorAuthenticationProvider();
        p.setBypassEvaluator(authyBypassEvaluator());
        return p;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass authyBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getAuthy().getBypass()
        );
    }
    
    @RefreshScope
    @Bean
    public CasWebflowEventResolver authyAuthenticationWebflowEventResolver() {
        final AuthyAuthenticationWebflowEventResolver r = new AuthyAuthenticationWebflowEventResolver();
        r.setAuthenticationSystemSupport(this.authenticationSystemSupport);
        r.setCentralAuthenticationService(this.centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(this.multifactorAuthenticationProviderSelector);
        r.setServicesManager(this.servicesManager);
        r.setTicketRegistrySupport(this.ticketRegistrySupport);
        r.setWarnCookieGenerator(this.warnCookieGenerator);
        return r;
    }

    @ConditionalOnMissingBean(name = "authyMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer authyMultifactorWebflowConfigurer() {
        final AuthyMultifactorWebflowConfigurer c =
                new AuthyMultifactorWebflowConfigurer();
        c.setFlowDefinitionRegistry(authyAuthenticatorFlowRegistry());
        c.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        c.setFlowBuilderServices(flowBuilderServices);
        return c;
    }

    @RefreshScope
    @Bean
    public Action authyAuthenticationWebflowAction() {
        final AuthyAuthenticationWebflowAction a = new AuthyAuthenticationWebflowAction();
        a.setCasWebflowEventResolver(authyAuthenticationWebflowEventResolver());
        return a;
    }

    @RefreshScope
    @Bean
    public AuthyClientInstance authyClientInstance() {
        if (StringUtils.isBlank(casProperties.getAuthn().getMfa().getAuthy().getApiKey())) {
            throw new IllegalArgumentException("Authy API key must be defined");
        }
        final AuthyClientInstance i = new AuthyClientInstance(
                casProperties.getAuthn().getMfa().getAuthy().getApiKey(),
                casProperties.getAuthn().getMfa().getAuthy().getApiUrl()
        );
        i.setMailAttribute(casProperties.getAuthn().getMfa().getAuthy().getMailAttribute());
        i.setPhoneAttribute(casProperties.getAuthn().getMfa().getAuthy().getPhoneAttribute());
        return i;
    }

    @RefreshScope
    @Bean
    public Action authyAuthenticationRegistrationWebflowAction() {
        return new AuthyAuthenticationRegistrationWebflowAction(authyClientInstance());
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        authenticationHandlersResolvers.put(authyAuthenticationHandler(), null);
        authenticationMetadataPopulators.add(0, authyAuthenticationMetaDataPopulator());
    }

    /**
     * The Authy multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.authy", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("authyMultifactorTrustConfiguration")
    public class AuthyMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "authyMultifactorTrustWebflowConfigurer")
        @Bean
        public CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer() {
            final AuthyMultifactorTrustWebflowConfigurer r = new AuthyMultifactorTrustWebflowConfigurer();
            r.setFlowDefinitionRegistry(authyAuthenticatorFlowRegistry());
            r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
            r.setFlowBuilderServices(flowBuilderServices);
            r.setEnableDeviceRegistration(casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled());
            return r;
        }
    }
}
