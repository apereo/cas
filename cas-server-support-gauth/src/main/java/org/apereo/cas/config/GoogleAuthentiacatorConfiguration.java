package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccountRegistry;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorApplicationContextWrapper;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationHandler;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorInstance;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.gauth.InMemoryGoogleAuthenticatorAccountRegistry;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorAuthenticationWebflowAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

/**
 * This is {@link GoogleAuthentiacatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthenticatorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleAuthentiacatorConfiguration {

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
    @Qualifier("builder")
    private FlowBuilderServices builder;

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

    /**
     * Yubikey flow registry flow definition registry.
     *
     * @return the flow definition registry
     */
    @RefreshScope
    @Bean
    public FlowDefinitionRegistry googleAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.builder);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-gauth/*-webflow.xml");
        return builder.build();
    }


    @Bean
    public BaseApplicationContextWrapper googleAuthenticatorApplicationContextWrapper() {
        final GoogleAuthenticatorApplicationContextWrapper w =
                new GoogleAuthenticatorApplicationContextWrapper();
        w.setAuthenticationHandler(googleAuthenticatorAuthenticationHandler());
        w.setPopulator(googleAuthenticatorAuthenticationMetaDataPopulator());
        return w;
    }

    @Bean
    public AuthenticationHandler googleAuthenticatorAuthenticationHandler() {
        final GoogleAuthenticatorAuthenticationHandler h = new GoogleAuthenticatorAuthenticationHandler();
        h.setAccountRegistry(defaultGoogleAuthenticatorAccountRegistry());
        h.setGoogleAuthenticatorInstance(googleAuthenticatorInstance());
        h.setPrincipalFactory(googlePrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public PrincipalFactory googlePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator() {
        final GoogleAuthenticatorAuthenticationMetaDataPopulator g =
                new GoogleAuthenticatorAuthenticationMetaDataPopulator(
                        casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                        googleAuthenticatorAuthenticationHandler(),
                        googleAuthenticatorAuthenticationProvider()
                );
        return g;
    }

    @Bean
    @RefreshScope
    public GoogleAuthenticatorInstance googleAuthenticatorInstance() {
        final GoogleAuthenticatorInstance i = new GoogleAuthenticatorInstance();
        return i;
    }

    @Bean
    @RefreshScope
    public AbstractMultifactorAuthenticationProvider googleAuthenticatorAuthenticationProvider() {
        return new GoogleAuthenticatorMultifactorAuthenticationProvider();
    }

    @Bean
    public GoogleAuthenticatorAccountRegistry defaultGoogleAuthenticatorAccountRegistry() {
        return new InMemoryGoogleAuthenticatorAccountRegistry();
    }

    @Bean
    public CasWebflowEventResolver googleAuthenticatorAuthenticationWebflowEventResolver() {
        final GoogleAuthenticatorAuthenticationWebflowEventResolver r = new GoogleAuthenticatorAuthenticationWebflowEventResolver();
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
        return r;
    }

    @Bean
    public Action saveAccountRegistrationAction() {
        final GoogleAccountSaveRegistrationAction a = new GoogleAccountSaveRegistrationAction();
        a.setAccountRegistry(defaultGoogleAuthenticatorAccountRegistry());
        return a;
    }

    @Bean
    public Action googleAuthenticatorAuthenticationWebflowAction() {
        final GoogleAuthenticatorAuthenticationWebflowAction a = new GoogleAuthenticatorAuthenticationWebflowAction();
        a.setCasWebflowEventResolver(googleAuthenticatorAuthenticationWebflowEventResolver());
        return a;
    }

    @Bean
    public CasWebflowConfigurer googleAuthenticatorMultifactorWebflowConfigurer() {
        final GoogleAuthenticatorMultifactorWebflowConfigurer c =
                new GoogleAuthenticatorMultifactorWebflowConfigurer();
        c.setFlowDefinitionRegistry(googleAuthenticatorFlowRegistry());
        c.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        c.setFlowBuilderServices(flowBuilderServices);
        return c;
    }

    @Bean
    @RefreshScope
    public Action googleAccountRegistrationAction() {
        final GoogleAccountCheckRegistrationAction a = new GoogleAccountCheckRegistrationAction();
        a.setAccountRegistry(defaultGoogleAuthenticatorAccountRegistry());
        a.setGoogleAuthenticatorInstance(googleAuthenticatorInstance());
        return a;
    }
}
