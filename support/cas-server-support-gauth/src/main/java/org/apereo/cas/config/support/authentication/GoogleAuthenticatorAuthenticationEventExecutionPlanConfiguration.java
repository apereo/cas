package org.apereo.cas.config.support.authentication;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.GoogleAuthenticatorAuthenticationHandler;
import org.apereo.cas.gauth.GoogleAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredentialRepositoryEndpoint;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.JsonGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.RestGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepositoryCleaner;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCheckRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationAction;
import org.apereo.cas.services.ServicesManager;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.execution.Action;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("googleAuthenticatorAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration {

    @Lazy
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private ObjectProvider<OneTimeTokenCredentialRepository> googleAuthenticatorAccountRegistry;

    @Autowired
    @Qualifier("googleAuthenticatorBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> googleAuthenticatorBypassEvaluator;

    @Lazy
    @Autowired
    @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
    private ObjectProvider<OneTimeTokenRepository> oneTimeTokenAuthenticatorTokenRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Bean
    public IGoogleAuthenticator googleAuthenticatorInstance() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();

        bldr.setCodeDigits(gauth.getCodeDigits());
        bldr.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(gauth.getTimeStepSize()));
        bldr.setWindowSize(gauth.getWindowSize());
        bldr.setKeyRepresentation(KeyRepresentation.BASE32);
        return new GoogleAuthenticator(bldr.build());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler googleAuthenticatorAuthenticationHandler() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        return new GoogleAuthenticatorAuthenticationHandler(gauth.getName(),
            servicesManager.getIfAvailable(),
            googlePrincipalFactory(),
            googleAuthenticatorInstance(),
            oneTimeTokenAuthenticatorTokenRepository.getIfAvailable(),
            googleAuthenticatorAccountRegistry.getIfAvailable(),
            gauth.getOrder());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val p = new GoogleAuthenticatorMultifactorAuthenticationProvider();
        p.setBypassEvaluator(googleAuthenticatorBypassEvaluator.getIfAvailable());
        p.setFailureMode(gauth.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getIfAvailable());
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
            googleAuthenticatorMultifactorAuthenticationProvider().getId()
        );
    }

    @Bean
    @RefreshScope
    public Action googleAccountRegistrationAction() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        return new OneTimeTokenAccountCheckRegistrationAction(googleAuthenticatorAccountRegistry.getIfAvailable(),
            gauth.getLabel(),
            gauth.getIssuer());
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth.cleaner", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @Autowired
    public OneTimeTokenRepositoryCleaner googleAuthenticatorTokenRepositoryCleaner(@Qualifier("oneTimeTokenAuthenticatorTokenRepository")
                                                                                   final OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository) {
        return new GoogleAuthenticatorOneTimeTokenRepositoryCleaner(oneTimeTokenAuthenticatorTokenRepository);
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
    @Bean
    @RefreshScope
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        if (gauth.getJson().getLocation() != null) {
            return new JsonGoogleAuthenticatorTokenCredentialRepository(gauth.getJson().getLocation(), googleAuthenticatorInstance(),
                googleAuthenticatorAccountCipherExecutor());
        }
        if (StringUtils.isNotBlank(gauth.getRest().getEndpointUrl())) {
            return new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance(),
                new RestTemplate(), gauth, googleAuthenticatorAccountCipherExecutor());
        }
        return new InMemoryGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorAccountCipherExecutor(), googleAuthenticatorInstance());
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public GoogleAuthenticatorTokenCredentialRepositoryEndpoint googleAuthenticatorTokenCredentialRepositoryEndpoint() {
        return new GoogleAuthenticatorTokenCredentialRepositoryEndpoint(casProperties, googleAuthenticatorAccountRegistry());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountCipherExecutor")
    @Bean
    @RefreshScope
    public CipherExecutor googleAuthenticatorAccountCipherExecutor() {
        val crypto = casProperties.getAuthn().getMfa().getGauth().getCrypto();
        if (crypto.isEnabled()) {
            return new OneTimeTokenAccountCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg(),
                crypto.getSigning().getKeySize(),
                crypto.getEncryption().getKeySize());
        }
        LOGGER.warn("Google Authenticator one-time token account encryption/signing is turned off. "
            + "Consider turning on encryption, signing to securely and safely store one-time token accounts.");
        return CipherExecutor.noOp();
    }

    @Bean
    @RefreshScope
    public Action googleSaveAccountRegistrationAction() {
        return new OneTimeTokenAccountSaveRegistrationAction(googleAuthenticatorAccountRegistry.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "googlePrincipalFactory")
    @Bean
    public PrincipalFactory googlePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer googleAuthenticatorAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getIssuer())) {
                plan.registerAuthenticationHandler(googleAuthenticatorAuthenticationHandler());
                plan.registerAuthenticationMetadataPopulator(googleAuthenticatorAuthenticationMetaDataPopulator());
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(GoogleAuthenticatorTokenCredential.class));
            }
        };
    }

    /**
     * The type Google authenticator one time token repository cleaner.
     */
    public static class GoogleAuthenticatorOneTimeTokenRepositoryCleaner extends OneTimeTokenRepositoryCleaner {
        public GoogleAuthenticatorOneTimeTokenRepositoryCleaner(final OneTimeTokenRepository tokenRepository) {
            super(tokenRepository);
        }

        @Scheduled(initialDelayString = "${cas.authn.mfa.gauth.cleaner.schedule.startDelay:PT30S}",
            fixedDelayString = "${cas.authn.mfa.gauth.cleaner.schedule.repeatInterval:PT35S}")
        @Override
        public void clean() {
            super.clean();
        }
    }
}
