package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.gauth.GoogleAuthenticatorAuthenticationHandler;
import org.apereo.cas.gauth.GoogleAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.GoogleAuthenticatorService;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorOneTimeTokenCredentialValidator;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredentialRepositoryEndpoint;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.JsonGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.RestGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.gauth.token.GoogleAuthenticatorTokenRepositoryCleaner;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorAccountCheckRegistrationAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorConfirmAccountRegistrationAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorDeleteAccountAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorPrepareLoginAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorSaveRegistrationAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorValidateSelectedRegistrationAction;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorValidateTokenAction;
import org.apereo.cas.gauth.web.flow.account.GoogleMultifactorAuthenticationAccountProfilePrepareAction;
import org.apereo.cas.gauth.web.flow.account.GoogleMultifactorAuthenticationAccountProfileRegistrationAction;
import org.apereo.cas.gauth.web.flow.account.GoogleMultifactorAuthenticationAccountProfileWebflowConfigurer;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountSerializer;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialDeviceManager;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.JasyptNumberCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.thread.Cleanable;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.DefaultMultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator)
@Configuration(value = "GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "GoogleAuthenticatorAuthenticationEventExecutionPlaHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorAuthenticationEventExecutionPlaHandlerConfiguration {
        @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler googleAuthenticatorAuthenticationHandler(
            @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
            final CasConfigurationProperties casProperties,
            @Qualifier("googlePrincipalFactory")
            final PrincipalFactory googlePrincipalFactory,
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val gauth = casProperties.getAuthn().getMfa().getGauth();
            return new GoogleAuthenticatorAuthenticationHandler(gauth.getName(), servicesManager,
                googlePrincipalFactory, googleAuthenticatorOneTimeTokenCredentialValidator,
                gauth.getOrder(), multifactorAuthenticationProvider);
        }

    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorMultifactorAuthenticationCoreConfiguration {
        private static final BeanCondition CONDITION_SCRATCH_CODE =
            BeanCondition.on("cas.authn.mfa.gauth.core.scratch-codes.encryption.key");

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "googleAuthenticatorInstance")
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
        public CipherExecutor googleAuthenticatorAccountCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getMfa().getGauth().getCrypto();
            if (crypto.isEnabled()) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, OneTimeTokenAccountCipherExecutor.class);
            }
            LOGGER.warn("Google Authenticator one-time token account encryption/signing is turned off. "
                + "Consider turning on encryption, signing to securely and safely store one-time token accounts.");
            return CipherExecutor.noOp();
        }

        @ConditionalOnMissingBean(name = "googleAuthenticatorScratchCodesCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CipherExecutor googleAuthenticatorScratchCodesCipherExecutor(final ApplicationContext applicationContext,
                                                                            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CipherExecutor.class)
                .when(CONDITION_SCRATCH_CODE.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val key = casProperties.getAuthn().getMfa().getGauth().getCore().getScratchCodes().getEncryption().getKey();
                    return new JasyptNumberCipherExecutor(key, "googleAuthenticatorScratchCodesCipherExecutor");
                })
                .otherwise(() -> {
                    LOGGER.warn("Google Authenticator scratch codes encryption key is not defined. "
                        + "Consider defining the encryption key to securely and safely store scratch codes.");
                    return CipherExecutor.noOp();
                })
                .get();
        }

        @ConditionalOnMissingBean(name = "googlePrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory googlePrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }

    @Configuration(value = "GoogleAuthenticatorAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorAuthenticationEventExecutionPlanMetadataConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationMetaDataPopulator")
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

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorProviderAuthenticationMetadataPopulator")
        public AuthenticationMetaDataPopulator googleAuthenticatorMultifactorProviderAuthenticationMetadataPopulator(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
            val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
                multifactorAuthenticationProvider, servicesManager);
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorMultifactorAuthenticationWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public GoogleAuthenticatorTokenCredentialRepositoryEndpoint googleAuthenticatorTokenCredentialRepositoryEndpoint(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final ObjectProvider<OneTimeTokenCredentialRepository> googleAuthenticatorAccountRegistry) {
            return new GoogleAuthenticatorTokenCredentialRepositoryEndpoint(
                casProperties, applicationContext, googleAuthenticatorAccountRegistry);
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorMultifactorAuthenticationPlanConfiguration {
        @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer googleAuthenticatorAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("googleAuthenticatorMultifactorProviderAuthenticationMetadataPopulator")
            final AuthenticationMetaDataPopulator googleAuthenticatorMultifactorProviderAuthenticationMetadataPopulator,
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorAuthenticationHandler")
            final AuthenticationHandler googleAuthenticatorAuthenticationHandler,
            @Qualifier("googleAuthenticatorAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator) {
            return plan -> {
                if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGauth().getCore().getIssuer())) {
                    plan.registerAuthenticationHandler(googleAuthenticatorAuthenticationHandler);
                    plan.registerAuthenticationMetadataPopulator(googleAuthenticatorAuthenticationMetaDataPopulator);
                    plan.registerAuthenticationMetadataPopulator(googleAuthenticatorMultifactorProviderAuthenticationMetadataPopulator);
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(GoogleAuthenticatorTokenCredential.class));
                }
            };
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationTokenConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorMultifactorAuthenticationTokenConfiguration {
        @ConditionalOnMissingBean(name = "googleAuthenticatorOneTimeTokenCredentialValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator(
            @Qualifier("googleAuthenticatorInstance")
            final IGoogleAuthenticator googleAuthenticatorInstance,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            @Qualifier(OneTimeTokenRepository.BEAN_NAME)
            final OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository) {
            return new GoogleAuthenticatorOneTimeTokenCredentialValidator(googleAuthenticatorInstance,
                oneTimeTokenAuthenticatorTokenRepository, googleAuthenticatorAccountRegistry);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Cleanable googleAuthenticatorTokenRepositoryCleaner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(OneTimeTokenRepository.BEAN_NAME)
            final OneTimeTokenRepository repository) {
            return BeanSupplier.of(Cleanable.class)
                .when(BeanCondition.on("cas.authn.mfa.gauth.cleaner.schedule.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> new GoogleAuthenticatorTokenRepositoryCleaner(repository))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "googleAuthenticatorDeviceManager")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationDeviceManager googleAuthenticatorDeviceManager(
            @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> googleAuthenticatorMultifactorAuthenticationProvider,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return new OneTimeTokenCredentialDeviceManager(googleAuthenticatorAccountRegistry,
                googleAuthenticatorMultifactorAuthenticationProvider);
        }

        @ConditionalOnMissingBean(name = BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorInstance")
            final IGoogleAuthenticator googleAuthenticatorInstance,
            @Qualifier("googleAuthenticatorAccountCipherExecutor")
            final CipherExecutor googleAuthenticatorAccountCipherExecutor,
            @Qualifier("googleAuthenticatorScratchCodesCipherExecutor")
            final CipherExecutor googleAuthenticatorScratchCodesCipherExecutor) {
            val gauth = casProperties.getAuthn().getMfa().getGauth();
            if (gauth.getJson().getLocation() != null) {
                return new JsonGoogleAuthenticatorTokenCredentialRepository(gauth.getJson().getLocation(),
                    googleAuthenticatorInstance, googleAuthenticatorAccountCipherExecutor,
                    googleAuthenticatorScratchCodesCipherExecutor,
                    new OneTimeTokenAccountSerializer(applicationContext));
            }
            if (StringUtils.isNotBlank(gauth.getRest().getUrl())) {
                return new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
                    gauth, googleAuthenticatorAccountCipherExecutor, googleAuthenticatorScratchCodesCipherExecutor);
            }
            return new InMemoryGoogleAuthenticatorTokenCredentialRepository(
                googleAuthenticatorAccountCipherExecutor, googleAuthenticatorScratchCodesCipherExecutor, googleAuthenticatorInstance);
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorMultifactorAuthenticationWebflowConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_SELECTED_REGISTRATION)
        public Action googleValidateSelectedRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(GoogleAuthenticatorValidateSelectedRegistrationAction::new)
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_SELECTED_REGISTRATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION)
        public Action googleSaveAccountRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleAuthenticatorSaveRegistrationAction(
                    googleAuthenticatorAccountRegistry, casProperties, validator))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_TOKEN)
        public Action googleValidateTokenAction(
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleAuthenticatorValidateTokenAction(casProperties,
                    googleAuthenticatorAccountRegistry, googleAuthenticatorOneTimeTokenCredentialValidator))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_TOKEN)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_PREPARE_LOGIN)
        public Action prepareGoogleAuthenticatorLoginAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleAuthenticatorPrepareLoginAction(casProperties, googleAuthenticatorAccountRegistry))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_PREPARE_LOGIN)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_CHECK_ACCOUNT_REGISTRATION)
        public Action googleAccountCheckRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleAuthenticatorAccountCheckRegistrationAction(googleAuthenticatorAccountRegistry, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_CHECK_ACCOUNT_REGISTRATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_SELECTION)
        public Action googleAccountConfirmSelectionAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new OneTimeTokenAccountConfirmSelectionRegistrationAction(googleAuthenticatorAccountRegistry))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_SELECTION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_REGISTRATION)
        public Action googleAccountConfirmRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleAuthenticatorConfirmAccountRegistrationAction(
                    googleAuthenticatorAccountRegistry, googleAuthenticatorOneTimeTokenCredentialValidator))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_REGISTRATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_DELETE_DEVICE)
        public Action googleAccountDeleteDeviceAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
            final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> googleAuthenticatorOneTimeTokenCredentialValidator,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleAuthenticatorDeleteAccountAction(
                    googleAuthenticatorAccountRegistry, googleAuthenticatorOneTimeTokenCredentialValidator))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_DELETE_DEVICE)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_CREATE_REGISTRATION)
        public Action googleAccountCreateRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            val gauth = casProperties.getAuthn().getMfa().getGauth().getCore();
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new OneTimeTokenAccountCreateRegistrationAction(
                    googleAuthenticatorAccountRegistry, gauth.getLabel(), gauth.getIssuer()))
                .withId(CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_CREATE_REGISTRATION)
                .build()
                .get();
        }
    }

    @Configuration(value = "GoogleAuthenticatorMultifactorAuthenticationProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorMultifactorAuthenticationProviderConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorAuthenticationProvider")
        public MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator,
            @Qualifier("googleAuthenticatorDeviceManager")
            final MultifactorAuthenticationDeviceManager googleAuthenticatorDeviceManager) {
            val gauth = casProperties.getAuthn().getMfa().getGauth();
            val provider = new GoogleAuthenticatorMultifactorAuthenticationProvider();
            provider.setBypassEvaluator(googleAuthenticatorBypassEvaluator);
            provider.setFailureMode(gauth.getFailureMode());
            provider.setFailureModeEvaluator(failureModeEvaluator);
            provider.setOrder(gauth.getRank());
            provider.setId(gauth.getId());
            provider.setDeviceManager(googleAuthenticatorDeviceManager);
            return provider;
        }
    }

    @Configuration(value = "GoogleAuthenticatorAccountProfileWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class GoogleAuthenticatorAccountProfileWebflowConfiguration {

        @ConditionalOnMissingBean(name = "googleAccountProfileWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer googleAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new GoogleMultifactorAuthenticationAccountProfileWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleAccountCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer googleAccountCasWebflowExecutionPlanConfigurer(
            @Qualifier("googleAccountProfileWebflowConfigurer")
            final CasWebflowConfigurer googleAccountProfileWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(googleAccountProfileWebflowConfigurer);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_DEVICE_PROVIDER)
        public MultifactorAuthenticationDeviceProviderAction googleAccountDeviceProviderAction(
            @Qualifier("googleAuthenticatorDeviceManager")
            final MultifactorAuthenticationDeviceManager googleAuthenticatorDeviceManager) {
            return new DefaultMultifactorAuthenticationDeviceProviderAction(googleAuthenticatorDeviceManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_PREPARE)
        public Action googleAccountProfilePrepareAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider,
            final CasConfigurationProperties casProperties,
            @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
            final OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleMultifactorAuthenticationAccountProfilePrepareAction(googleAuthenticatorAccountRegistry,
                            googleAuthenticatorMultifactorAuthenticationProvider, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_PREPARE)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_REGISTRATION)
        public Action googleAccountProfileRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new GoogleMultifactorAuthenticationAccountProfileRegistrationAction(googleAuthenticatorMultifactorAuthenticationProvider))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_REGISTRATION)
                .build()
                .get();
        }

    }
}
