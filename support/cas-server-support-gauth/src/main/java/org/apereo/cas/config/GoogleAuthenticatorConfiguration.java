package org.apereo.cas.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationHandler;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.gauth.repository.credentials.InMemoryGoogleAuthenticatorCredentialRepository;
import org.apereo.cas.adaptors.gauth.repository.credentials.JsonGoogleAuthenticatorCredentialRepository;
import org.apereo.cas.adaptors.gauth.repository.token.CachingGoogleAuthenticatorTokenRepository;
import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorToken;
import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorTokenRepository;
import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorTokenRepositoryCleaner;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorAuthenticationWebflowAction;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.gauth.web.flow.rest.GoogleAuthenticatorQRGeneratorController;
import org.apereo.cas.authentication.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleAuthenticatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthenticatorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class GoogleAuthenticatorConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorConfiguration.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1_000_000;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private ICredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    @Qualifier("googleAuthenticatorTokenRepository")
    private GoogleAuthenticatorTokenRepository googleAuthenticatorTokenRepository;

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
    @Qualifier("authenticationMetadataPopulators")
    private List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators;

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
        final GoogleAuthenticatorAuthenticationHandler h = new GoogleAuthenticatorAuthenticationHandler(
                googleAuthenticatorInstance(), googleAuthenticatorTokenRepository);
        h.setPrincipalFactory(googlePrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setName(casProperties.getAuthn().getMfa().getGauth().getName());
        return h;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass googleBypassEvaluator() {
        return new DefaultMultifactorAuthenticationProviderBypass(
                casProperties.getAuthn().getMfa().getGauth().getBypass(),
                ticketRegistrySupport
        );
    }

    @ConditionalOnMissingBean(name = "googlePrincipalFactory")
    @Bean
    public PrincipalFactory googlePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                googleAuthenticatorAuthenticationHandler(),
                googleAuthenticatorAuthenticationProvider()
        );
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
    @Bean
    @RefreshScope
    public ICredentialRepository googleAuthenticatorAccountRegistry() {
        final MultifactorAuthenticationProperties.GAuth gauth = casProperties.getAuthn().getMfa().getGauth();
        if (gauth.getJson().getConfig().getLocation() != null) {
            return new JsonGoogleAuthenticatorCredentialRepository(gauth.getJson().getConfig().getLocation());
        }
        return new InMemoryGoogleAuthenticatorCredentialRepository();
    }

    @Bean
    @RefreshScope
    public IGoogleAuthenticator googleAuthenticatorInstance() {
        final MultifactorAuthenticationProperties.GAuth gauth = casProperties.getAuthn().getMfa().getGauth();
        final GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();

        bldr.setCodeDigits(gauth.getCodeDigits());
        bldr.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(gauth.getTimeStepSize()));
        bldr.setWindowSize(gauth.getWindowSize());
        bldr.setKeyRepresentation(KeyRepresentation.BASE32);

        final GoogleAuthenticator g = new GoogleAuthenticator(bldr.build());
        g.setCredentialRepository(googleAuthenticatorAccountRegistry);
        return g;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider googleAuthenticatorAuthenticationProvider() {
        final MultifactorAuthenticationProperties.GAuth gauth = casProperties.getAuthn().getMfa().getGauth();
        final GoogleAuthenticatorMultifactorAuthenticationProvider p = new GoogleAuthenticatorMultifactorAuthenticationProvider();
        p.setBypassEvaluator(googleBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(gauth.getRank());
        p.setId(gauth.getId());
        return p;
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver googleAuthenticatorAuthenticationWebflowEventResolver() {
        return new GoogleAuthenticatorAuthenticationWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService,
                servicesManager,
                ticketRegistrySupport,
                warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                multifactorAuthenticationProviderSelector);
    }

    @Bean
    @RefreshScope
    public Action saveAccountRegistrationAction() {
        return new GoogleAccountSaveRegistrationAction(googleAuthenticatorInstance());
    }

    @Bean
    @RefreshScope
    public Action googleAuthenticatorAuthenticationWebflowAction() {
        return new GoogleAuthenticatorAuthenticationWebflowAction(googleAuthenticatorAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer googleAuthenticatorMultifactorWebflowConfigurer() {
        return new GoogleAuthenticatorMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, googleAuthenticatorFlowRegistry());
    }

    @Bean
    @RefreshScope
    public Action googleAccountRegistrationAction() {
        return new GoogleAccountCheckRegistrationAction(googleAuthenticatorInstance(), casProperties.getAuthn().getMfa().getGauth());
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getIssuer())) {
            authenticationMetadataPopulators.add(0, googleAuthenticatorAuthenticationMetaDataPopulator());
        }
    }

    @Bean
    public GoogleAuthenticatorQRGeneratorController googleAuthenticatorQRGeneratorController() {
        return new GoogleAuthenticatorQRGeneratorController();
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorTokenRepository")
    @Bean
    public GoogleAuthenticatorTokenRepository googleAuthenticatorTokenRepository() {
        final LoadingCache<String, Collection<GoogleAuthenticatorToken>> storage = CacheBuilder.newBuilder()
                .initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .expireAfterWrite(casProperties.getAuthn().getMfa().getGauth().getTimeStepSize() * 2, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Collection<GoogleAuthenticatorToken>>() {
                    @Override
                    public Collection<GoogleAuthenticatorToken> load(final String s) throws Exception {
                        LOGGER.error("Load operation of the cache is not supported.");
                        return null;
                    }
                });
        return new CachingGoogleAuthenticatorTokenRepository(storage);
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth.cleaner", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public GoogleAuthenticatorTokenRepositoryCleaner googleAuthenticatorTokenRepositoryCleaner() {
        return new GoogleAuthenticatorTokenRepositoryCleaner(googleAuthenticatorTokenRepository);
    }

    /**
     * The type Google authenticator authentication event execution plan configuration.
     */
    @Configuration("googleAuthenticatorAuthenticationEventExecutionPlanConfiguration")
    public class GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getIssuer())) {
                plan.registerAuthenticationHandler(googleAuthenticatorAuthenticationHandler());
            }
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
            return new GoogleAuthenticatorMultifactorTrustWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                    casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(), loginFlowDefinitionRegistry);
        }
    }
}
