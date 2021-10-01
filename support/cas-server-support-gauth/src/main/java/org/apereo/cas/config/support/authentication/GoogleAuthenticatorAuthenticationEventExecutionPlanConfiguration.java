package org.apereo.cas.config.support.authentication;

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
import org.apereo.cas.gauth.GoogleAuthenticatorService;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorOneTimeTokenCredentialValidator;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredentialRepositoryEndpoint;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.JsonGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.RestGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorDeleteAccountAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorPrepareLoginAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorSaveRegistrationAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorValidateSelectedRegistrationAction;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepositoryCleaner;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCheckRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.webflow.execution.Action;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "googleAuthenticatorAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration {
    
    @Configuration(value = "GoogleAuthenticatorAuthenticationEventExecutionPlaHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorAuthenticationEventExecutionPlaHandlerConfiguration {
        @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationHandler googleAuthenticatorAuthenticationHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("googlePrincipalFactory")
            final PrincipalFactory googlePrincipalFactory,
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val gauth = casProperties.getAuthn().getMfa().getGauth();
            return new GoogleAuthenticatorAuthenticationHandler(gauth.getName(), servicesManager,
                googlePrincipalFactory, googleAuthenticatorOneTimeTokenCredentialValidator, gauth.getOrder());
        }

    }
    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorAuthenticationCoreConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "googleAuthenticatorInstance")
        @Autowired
        public IGoogleAuthenticator googleAuthenticatorInstance(final CasConfigurationProperties casProperties) {
            val gauth = casProperties.getAuthn().getMfa().getGauth().getCore();
            val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
            bldr.setCodeDigits(gauth.getCodeDigits());
            bldr.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(gauth.getTimeStepSize()));
            bldr.setWindowSize(gauth.getWindowSize());
            bldr.setKeyRepresentation(KeyRepresentation.BASE32);
            return new GoogleAuthenticatorService(new GoogleAuthenticator(bldr.build()));
        }

        @ConditionalOnMissingBean(name = "googleAuthenticatorAccountCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CipherExecutor googleAuthenticatorAccountCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getMfa().getGauth().getCrypto();
            if (crypto.isEnabled()) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, OneTimeTokenAccountCipherExecutor.class);
            }
            LOGGER.warn("Google Authenticator one-time token account encryption/signing is turned off. "
                        + "Consider turning on encryption, signing to securely and safely store one-time token accounts.");
            return CipherExecutor.noOp();
        }

        @ConditionalOnMissingBean(name = "googlePrincipalFactory")
        @Bean
        public PrincipalFactory googlePrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }
    
    @Configuration(value = "GoogleAuthenticatorAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorAuthenticationEventExecutionPlanMetadataConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationMetaDataPopulator")
        @Autowired
        public AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorAuthenticationHandler")
            final AuthenticationHandler googleAuthenticatorAuthenticationHandler,
            @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider) {
            return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), googleAuthenticatorAuthenticationHandler,
                googleAuthenticatorMultifactorAuthenticationProvider.getId());
        }

    }
    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorAuthenticationWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public GoogleAuthenticatorTokenCredentialRepositoryEndpoint googleAuthenticatorTokenCredentialRepositoryEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return new GoogleAuthenticatorTokenCredentialRepositoryEndpoint(casProperties, googleAuthenticatorAccountRegistry);
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorAuthenticationPlanConfiguration {
        @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @Autowired
        public AuthenticationEventExecutionPlanConfigurer googleAuthenticatorAuthenticationEventExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorAuthenticationHandler")
            final AuthenticationHandler googleAuthenticatorAuthenticationHandler,
            @Qualifier("googleAuthenticatorAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator) {
            return plan -> {
                if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getCore().getIssuer())) {
                    plan.registerAuthenticationHandler(googleAuthenticatorAuthenticationHandler);
                    plan.registerAuthenticationMetadataPopulator(googleAuthenticatorAuthenticationMetaDataPopulator);
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(GoogleAuthenticatorTokenCredential.class));
                }
            };
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorAuthenticationTokenConfiguration {
        @ConditionalOnMissingBean(name = "googleAuthenticatorOneTimeTokenCredentialValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator(
            @Qualifier("googleAuthenticatorInstance")
            final IGoogleAuthenticator googleAuthenticatorInstance,
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
            final OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository) {
            return new GoogleAuthenticatorOneTimeTokenCredentialValidator(googleAuthenticatorInstance,
                oneTimeTokenAuthenticatorTokenRepository, googleAuthenticatorAccountRegistry);
        }

        @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth.cleaner.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
        @Bean
        @Autowired
        public OneTimeTokenRepositoryCleaner googleAuthenticatorTokenRepositoryCleaner(
            @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
            final OneTimeTokenRepository repository) {
            return new GoogleAuthenticatorOneTimeTokenRepositoryCleaner(repository);
        }

        @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorInstance")
            final IGoogleAuthenticator googleAuthenticatorInstance,
            @Qualifier("googleAuthenticatorAccountCipherExecutor")
            final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
            val gauth = casProperties.getAuthn().getMfa().getGauth();
            if (gauth.getJson().getLocation() != null) {
                return new JsonGoogleAuthenticatorTokenCredentialRepository(gauth.getJson().getLocation(),
                    googleAuthenticatorInstance, googleAuthenticatorAccountCipherExecutor);
            }
            if (StringUtils.isNotBlank(gauth.getRest().getUrl())) {
                return new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
                    gauth, googleAuthenticatorAccountCipherExecutor);
            }
            return new InMemoryGoogleAuthenticatorTokenCredentialRepository(
                googleAuthenticatorAccountCipherExecutor, googleAuthenticatorInstance);
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorAuthenticationWebflowConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "validateSelectedRegistrationAction")
        public Action validateSelectedRegistrationAction() {
            return new GoogleAuthenticatorValidateSelectedRegistrationAction();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleSaveAccountRegistrationAction")
        @Autowired
        public Action googleSaveAccountRegistrationAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator) {
            return new GoogleAuthenticatorSaveRegistrationAction(googleAuthenticatorAccountRegistry, casProperties, validator);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "prepareGoogleAuthenticatorLoginAction")
        @Autowired
        public Action prepareGoogleAuthenticatorLoginAction(final CasConfigurationProperties casProperties) {
            return new GoogleAuthenticatorPrepareLoginAction(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAccountCheckRegistrationAction")
        public Action googleAccountCheckRegistrationAction(
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return new OneTimeTokenAccountCheckRegistrationAction(googleAuthenticatorAccountRegistry);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAccountConfirmSelectionAction")
        public Action googleAccountConfirmSelectionAction(
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return new OneTimeTokenAccountConfirmSelectionRegistrationAction(googleAuthenticatorAccountRegistry);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAccountDeleteDeviceAction")
        public Action googleAccountDeleteDeviceAction(
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return new GoogleAuthenticatorDeleteAccountAction(googleAuthenticatorAccountRegistry);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAccountCreateRegistrationAction")
        @Autowired
        public Action googleAccountCreateRegistrationAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorAccountRegistry")
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            val gauth = casProperties.getAuthn().getMfa().getGauth().getCore();
            return new OneTimeTokenAccountCreateRegistrationAction(googleAuthenticatorAccountRegistry, gauth.getLabel(), gauth.getIssuer());
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorAuthenticationProviderConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorAuthenticationProvider")
        @Autowired
        public MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            val gauth = casProperties.getAuthn().getMfa().getGauth();
            val p = new GoogleAuthenticatorMultifactorAuthenticationProvider();
            p.setBypassEvaluator(googleAuthenticatorBypassEvaluator);
            p.setFailureMode(gauth.getFailureMode());
            p.setFailureModeEvaluator(failureModeEvaluator);
            p.setOrder(gauth.getRank());
            p.setId(gauth.getId());
            return p;
        }
    }

    /**
     * The type Google authenticator one time token repository cleaner.
     */
    public static class GoogleAuthenticatorOneTimeTokenRepositoryCleaner extends OneTimeTokenRepositoryCleaner {

        public GoogleAuthenticatorOneTimeTokenRepositoryCleaner(final OneTimeTokenRepository tokenRepository) {
            super(tokenRepository);
        }

        @Scheduled(initialDelayString = "${cas.authn.mfa.gauth.cleaner.schedule.start-delay:PT30S}", fixedDelayString = "${cas.authn.mfa.gauth.cleaner.schedule.repeat-interval:PT35S}")
        @Override
        public void clean() {
            super.clean();
        }
    }
}
