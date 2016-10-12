package org.apereo.cas.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationHandler;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.gauth.InMemoryGoogleAuthenticatorAccountRegistry;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorAuthenticationWebflowAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
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
import java.util.concurrent.TimeUnit;

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
    @Qualifier("googleAuthenticatorAccountRegistry")
    private ICredentialRepository googleAuthenticatorAccountRegistry;

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
    public FlowDefinitionRegistry googleAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-gauth/*-webflow.xml");
        return builder.build();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler googleAuthenticatorAuthenticationHandler() {
        final GoogleAuthenticatorAuthenticationHandler h = new GoogleAuthenticatorAuthenticationHandler();
        h.setGoogleAuthenticatorInstance(googleAuthenticatorInstance());
        h.setPrincipalFactory(googlePrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass googleBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getGauth().getBypass()
        );
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

    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
    @Bean
    @RefreshScope
    public ICredentialRepository googleAuthenticatorAccountRegistry() {
        return new InMemoryGoogleAuthenticatorAccountRegistry();
    }

    @Bean
    @RefreshScope
    public IGoogleAuthenticator googleAuthenticatorInstance() {
        final GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder bldr =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();

        bldr.setCodeDigits(casProperties.getAuthn().getMfa().getGauth().getCodeDigits());
        bldr.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(casProperties.getAuthn().getMfa().getGauth().getTimeStepSize()));
        bldr.setWindowSize(casProperties.getAuthn().getMfa().getGauth().getWindowSize());
        bldr.setKeyRepresentation(KeyRepresentation.BASE32);

        final GoogleAuthenticator g = new GoogleAuthenticator(bldr.build());
        g.setCredentialRepository(googleAuthenticatorAccountRegistry);
        return g;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider googleAuthenticatorAuthenticationProvider() {
        final GoogleAuthenticatorMultifactorAuthenticationProvider p = new GoogleAuthenticatorMultifactorAuthenticationProvider();
        p.setBypassEvaluator(googleBypassEvaluator());
        return p;
    }

    @Bean
    @RefreshScope
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
    @RefreshScope
    public Action saveAccountRegistrationAction() {
        final GoogleAccountSaveRegistrationAction a = new GoogleAccountSaveRegistrationAction();
        a.setGoogleAuthenticator(googleAuthenticatorInstance());
        return a;
    }

    @Bean
    @RefreshScope
    public Action googleAuthenticatorAuthenticationWebflowAction() {
        final GoogleAuthenticatorAuthenticationWebflowAction a = new GoogleAuthenticatorAuthenticationWebflowAction();
        a.setCasWebflowEventResolver(googleAuthenticatorAuthenticationWebflowEventResolver());
        return a;
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorWebflowConfigurer")
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
        a.setGoogleAuthenticatorInstance(googleAuthenticatorInstance());
        return a;
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getIssuer())) {
            authenticationHandlersResolvers.put(googleAuthenticatorAuthenticationHandler(), null);
            authenticationMetadataPopulators.add(0, googleAuthenticatorAuthenticationMetaDataPopulator());
        }
    }

    /**
     * The google authenticator multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("gauthMultifactorTrustConfiguration")
    public class GoogleAuthenticatorMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "gauthMultifactorTrustWebflowConfigurer")
        @Bean
        public CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer() {
            final GoogleAuthenticatorMultifactorTrustWebflowConfigurer r = new GoogleAuthenticatorMultifactorTrustWebflowConfigurer();
            r.setFlowDefinitionRegistry(googleAuthenticatorFlowRegistry());
            r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
            r.setFlowBuilderServices(flowBuilderServices);
            r.setEnableDeviceRegistration(casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled());
            return r;
        }
    }
}
