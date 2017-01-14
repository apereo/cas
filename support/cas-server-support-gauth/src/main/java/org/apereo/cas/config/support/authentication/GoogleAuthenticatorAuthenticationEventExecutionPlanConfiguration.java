package org.apereo.cas.config.support.authentication;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.apache.commons.lang3.StringUtils;
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
import org.apereo.cas.authentication.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("googleAuthenticatorAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1_000;

    @Lazy
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private ICredentialRepository googleAuthenticatorAccountRegistry;

    @Lazy
    @Autowired
    @Qualifier("googleAuthenticatorTokenRepository")
    private GoogleAuthenticatorTokenRepository googleAuthenticatorTokenRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

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
    public AuthenticationHandler googleAuthenticatorAuthenticationHandler() {
        final GoogleAuthenticatorAuthenticationHandler h = new GoogleAuthenticatorAuthenticationHandler(
                googleAuthenticatorInstance(),
                googleAuthenticatorTokenRepository);
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
    public AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                googleAuthenticatorAuthenticationHandler(),
                googleAuthenticatorAuthenticationProvider()
        );
    }

    @Bean
    @RefreshScope
    public Action googleAccountRegistrationAction() {
        return new GoogleAccountCheckRegistrationAction(googleAuthenticatorInstance(), casProperties.getAuthn().getMfa().getGauth());
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth.cleaner", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @Autowired
    public GoogleAuthenticatorTokenRepositoryCleaner googleAuthenticatorTokenRepositoryCleaner(
            @Qualifier("googleAuthenticatorTokenRepository")
            final GoogleAuthenticatorTokenRepository googleAuthenticatorTokenRepository) {
        return new GoogleAuthenticatorTokenRepositoryCleaner(googleAuthenticatorTokenRepository);
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
    public Action saveAccountRegistrationAction() {
        return new GoogleAccountSaveRegistrationAction(googleAuthenticatorInstance());
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getIssuer())) {
            authenticationMetadataPopulators.add(0, googleAuthenticatorAuthenticationMetaDataPopulator());
        }
    }

    @ConditionalOnMissingBean(name = "googlePrincipalFactory")
    @Bean
    public PrincipalFactory googlePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getIssuer())) {
            plan.registerAuthenticationHandler(googleAuthenticatorAuthenticationHandler());
        }
    }
}
