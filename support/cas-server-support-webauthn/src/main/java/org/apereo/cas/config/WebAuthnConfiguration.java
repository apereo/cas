package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.thread.Cleanable;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.CasWebSecurityConstants;
import org.apereo.cas.webauthn.WebAuthnAuthenticationHandler;
import org.apereo.cas.webauthn.WebAuthnCredential;
import org.apereo.cas.webauthn.WebAuthnCredentialRegistrationCipherExecutor;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;
import org.apereo.cas.webauthn.metadata.CompositeAttestationTrustSource;
import org.apereo.cas.webauthn.storage.JsonResourceWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.web.WebAuthnController;
import org.apereo.cas.webauthn.web.WebAuthnRegisteredDevicesEndpoint;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.yubico.core.DefaultSessionManager;
import com.yubico.core.InMemoryRegistrationStorage;
import com.yubico.core.SessionManager;
import com.yubico.core.WebAuthnServer;
import com.yubico.fido.metadata.FidoMetadataDownloader;
import com.yubico.fido.metadata.FidoMetadataService;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.attestation.AttestationTrustSource;
import com.yubico.webauthn.attestation.MetadataObject;
import com.yubico.webauthn.attestation.YubicoJsonMetadataService;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.extension.appid.AppId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This is {@link WebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@Configuration(value = "WebAuthnConfiguration", proxyBeanMethods = false)
class WebAuthnConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.web-authn.core.enabled").isTrue().evenIfMissing();

    private static final int CACHE_MAX_SIZE = 10_000;

    private static <K, V> Cache<K, V> newCache() {
        return Caffeine.newBuilder().maximumSize(CACHE_MAX_SIZE)
            .expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    @Configuration(value = "WebAuthnMetadataServiceConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnMetadataServiceConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webAuthnMetadataService")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public AttestationTrustSource webAuthnMetadataService(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AttestationTrustSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val composite = new CompositeAttestationTrustSource();

                    val trustSource = casProperties.getAuthn().getMfa().getWebAuthn().getCore().getTrustSource();

                    val loc = trustSource.getTrustedDeviceMetadata().getLocation();
                    if (ResourceUtils.doesResourceExist(loc)) {
                        LOGGER.debug("Loading FIDO trusted device metadata from location [{}]", loc);
                        val metadata = MetadataObject.readMetadata(loc.getInputStream());
                        val jsonService = new YubicoJsonMetadataService(List.of(metadata));
                        composite.addAttestationTrustSource(jsonService);
                    }

                    val fidoProperties = trustSource.getFido();
                    if (StringUtils.isNotBlank(fidoProperties.getLegalHeader())
                        && StringUtils.isNotBlank(fidoProperties.getMetadataBlobUrl())
                        && StringUtils.isNotBlank(fidoProperties.getTrustRootUrl())) {
                        val trustRootUrl = new URI(fidoProperties.getTrustRootUrl()).toURL();
                        val trustRootUrlHashes = org.springframework.util.StringUtils.commaDelimitedListToSet(fidoProperties.getTrustRootHash())
                            .stream()
                            .map(Unchecked.function(ByteArray::fromHex))
                            .collect(Collectors.toSet());

                        val downloader = FidoMetadataDownloader.builder()
                            .expectLegalHeader(fidoProperties.getLegalHeader())
                            .downloadTrustRoot(trustRootUrl, trustRootUrlHashes)
                            .useTrustRootCacheFile(fidoProperties.getTrustRootCacheFile())
                            .downloadBlob(new URI(fidoProperties.getMetadataBlobUrl()).toURL())
                            .useBlobCacheFile(fidoProperties.getBlobCacheFile())
                            .verifyDownloadsOnly(true)
                            .clock(Clock.systemUTC())
                            .build();

                        LOGGER.info("You have chosen to accept the FIDO Alliance's legal terms & conditions for downloading metadata blobs");
                        LOGGER.info(fidoProperties.getLegalHeader());

                        LOGGER.debug("Starting to refresh/download FIDO metadata blob from [{}] and caching it at [{}]",
                            fidoProperties.getMetadataBlobUrl(), fidoProperties.getBlobCacheFile());
                        val blob = downloader.refreshBlob();
                        val fidoService = FidoMetadataService.builder()
                            .useBlob(blob)
                            .build();
                        composite.addAttestationTrustSource(fidoService);
                    }
                    return composite;
                }))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "WebAuthnCoreConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnCoreConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnSessionManager")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public SessionManager webAuthnSessionManager(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(SessionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(DefaultSessionManager::new)
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "webAuthnPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory webAuthnPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "WebAuthnSchedulerConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnSchedulerConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnDeviceRepositoryCleanerScheduler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Cleanable webAuthnDeviceRepositoryCleanerScheduler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final WebAuthnCredentialRepository webAuthnCredentialRepository) throws Exception {
            return BeanSupplier.of(Cleanable.class)
                .when(BeanCondition.on("cas.authn.mfa.web-authn.cleaner.schedule.enabled").isTrue().evenIfMissing().given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnDeviceRepositoryCleanerScheduler(webAuthnCredentialRepository))
                .otherwiseProxy()
                .get();
        }
    }

    @RequiredArgsConstructor
    static class WebAuthnDeviceRepositoryCleanerScheduler implements Cleanable {

        private final WebAuthnCredentialRepository repository;

        @Scheduled(initialDelayString = "${cas.authn.mfa.web-authn.cleaner.schedule.start-delay:PT20S}",
            fixedDelayString = "${cas.authn.mfa.web-authn.cleaner.schedule.repeat-interval:PT5M}")
        @Override
        public void clean() {
            LOGGER.debug("Starting to clean expired devices from repository");
            repository.clean();
        }
    }

    @Configuration(value = "WebAuthnServerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnServerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webAuthnServer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebAuthnServer webAuthnServer(
            final CasConfigurationProperties casProperties,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final WebAuthnCredentialRepository webAuthnCredentialRepository,
            @Qualifier("webAuthnMetadataService") final AttestationTrustSource webAuthnMetadataService,
            @Qualifier("webAuthnSessionManager") final SessionManager webAuthnSessionManager) throws Exception {

            val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn().getCore();
            val serverName = casProperties.getServer().getName();
            val defaultRelyingPartyId = RelyingPartyIdentity
                .builder()
                .id(StringUtils.defaultIfBlank(webAuthn.getRelyingPartyId(), new URI(serverName).toURL().getHost()))
                .name(StringUtils.defaultIfBlank(webAuthn.getRelyingPartyName(), "CAS"))
                .build();

            val origins = new LinkedHashSet<String>();
            origins.add(serverName);
            if (StringUtils.isNotBlank(webAuthn.getAllowedOrigins())) {
                origins.addAll(org.springframework.util.StringUtils.commaDelimitedListToSet(webAuthn.getAllowedOrigins()));
            }

            val conveyance = AttestationConveyancePreference.valueOf(webAuthn.getAttestationConveyancePreference().toUpperCase(Locale.ENGLISH));
            val appId = new AppId(StringUtils.defaultIfBlank(webAuthn.getApplicationId(), serverName));
            val relyingParty = RelyingParty.builder()
                .identity(defaultRelyingPartyId)
                .credentialRepository(webAuthnCredentialRepository)
                .origins(origins)
                .attestationConveyancePreference(conveyance)
                .attestationTrustSource(webAuthnMetadataService)
                .allowUntrustedAttestation(webAuthn.isAllowUntrustedAttestation())
                .validateSignatureCounter(webAuthn.isValidateSignatureCounter())
                .appId(appId)
                .build();
            return new WebAuthnServer(webAuthnCredentialRepository,
                newCache(), newCache(), relyingParty,
                webAuthnSessionManager, casProperties);
        }
    }

    @Configuration(value = "WebAuthnHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnHandlerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "webAuthnAuthenticationHandler")
        public AuthenticationHandler webAuthnAuthenticationHandler(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("webAuthnPrincipalFactory") final PrincipalFactory webAuthnPrincipalFactory,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final WebAuthnCredentialRepository webAuthnCredentialRepository,
            @Qualifier("webAuthnMultifactorAuthenticationProvider") final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
            @Qualifier("webAuthnSessionManager") final SessionManager webAuthnSessionManager,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return BeanSupplier.of(AuthenticationHandler.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
                    return new WebAuthnAuthenticationHandler(webAuthn.getName(),
                        servicesManager, webAuthnPrincipalFactory,
                        webAuthnCredentialRepository, webAuthnSessionManager,
                        webAuthn.getOrder(), multifactorAuthenticationProvider);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "WebAuthnMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnMetadataConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "webAuthnMultifactorProviderAuthenticationMetadataPopulator")
        public AuthenticationMetaDataPopulator webAuthnMultifactorProviderAuthenticationMetadataPopulator(
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("webAuthnMultifactorAuthenticationProvider") final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
            val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
                multifactorAuthenticationProvider, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "webAuthnAuthenticationMetaDataPopulator")
        public AuthenticationMetaDataPopulator webAuthnAuthenticationMetaDataPopulator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("webAuthnAuthenticationHandler") final AuthenticationHandler webAuthnAuthenticationHandler,
            @Qualifier("webAuthnMultifactorAuthenticationProvider") final MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider) {
            return BeanSupplier.of(AuthenticationMetaDataPopulator.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
                    return new AuthenticationContextAttributeMetaDataPopulator(authenticationContextAttribute,
                        webAuthnAuthenticationHandler, webAuthnMultifactorAuthenticationProvider.getId());
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "WebAuthnAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnAuthenticationPlanConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer webAuthnAuthenticationEventExecutionPlanConfigurer(
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnMultifactorProviderAuthenticationMetadataPopulator") final AuthenticationMetaDataPopulator webAuthnMultifactorProviderAuthenticationMetadataPopulator,
            @Qualifier("webAuthnAuthenticationHandler") final AuthenticationHandler webAuthnAuthenticationHandler,
            @Qualifier("webAuthnAuthenticationMetaDataPopulator") final AuthenticationMetaDataPopulator webAuthnAuthenticationMetaDataPopulator) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    plan.registerAuthenticationHandlerWithPrincipalResolver(webAuthnAuthenticationHandler, defaultPrincipalResolver);
                    plan.registerAuthenticationMetadataPopulator(webAuthnAuthenticationMetaDataPopulator);
                    plan.registerAuthenticationMetadataPopulator(webAuthnMultifactorProviderAuthenticationMetadataPopulator);
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(WebAuthnCredential.class));
                })
                .otherwiseProxy()
                .get();

        }
    }

    @Configuration(value = "WebAuthnRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnRepositoryConfiguration {
        @ConditionalOnMissingBean(name = WebAuthnCredentialRepository.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebAuthnCredentialRepository webAuthnCredentialRepository(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("webAuthnCredentialRegistrationCipherExecutor") final CipherExecutor webAuthnCredentialRegistrationCipherExecutor) throws Exception {
            return BeanSupplier.of(WebAuthnCredentialRepository.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val webauthn = casProperties.getAuthn().getMfa().getWebAuthn();
                    val location = webauthn.getJson().getLocation();
                    return FunctionUtils.doIfNotNull(location,
                        () -> new JsonResourceWebAuthnCredentialRepository(casProperties, location, webAuthnCredentialRegistrationCipherExecutor),
                        () -> new InMemoryRegistrationStorage(casProperties, webAuthnCredentialRegistrationCipherExecutor)).get();
                })
                .otherwiseProxy()
                .get();
        }

        @Configuration(value = "WebAuthnMultifactorProviderConfiguration", proxyBeanMethods = false)
        @EnableConfigurationProperties(CasConfigurationProperties.class)
        static class WebAuthnMultifactorProviderConfiguration {
            @ConditionalOnMissingBean(name = "webAuthnMultifactorAuthenticationProvider")
            @Bean
            @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
            public MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider(
                final ConfigurableApplicationContext applicationContext,
                final CasConfigurationProperties casProperties,
                @Qualifier("failureModeEvaluator") final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator,
                @Qualifier("webAuthnBypassEvaluator") final MultifactorAuthenticationProviderBypassEvaluator webAuthnBypassEvaluator) throws Exception {
                return BeanSupplier.of(MultifactorAuthenticationProvider.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> {
                        val webauthn = casProperties.getAuthn().getMfa().getWebAuthn();
                        val provider = new WebAuthnMultifactorAuthenticationProvider();
                        provider.setBypassEvaluator(webAuthnBypassEvaluator);
                        provider.setFailureMode(webauthn.getFailureMode());
                        provider.setFailureModeEvaluator(failureModeEvaluator);
                        provider.setOrder(webauthn.getRank());
                        provider.setId(webauthn.getId());
                        return provider;
                    })
                    .otherwiseProxy()
                    .get();
            }
        }

        @Configuration(value = "WebAuthnControllerConfiguration", proxyBeanMethods = false)
        @EnableConfigurationProperties(CasConfigurationProperties.class)
        static class WebAuthnControllerConfiguration {
            @Bean
            @ConditionalOnAvailableEndpoint
            @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
            public WebAuthnRegisteredDevicesEndpoint webAuthnRegisteredDevicesEndpoint(
                final CasConfigurationProperties casProperties,
                @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final ObjectProvider<WebAuthnCredentialRepository> webAuthnCredentialRepository) {
                return new WebAuthnRegisteredDevicesEndpoint(casProperties, webAuthnCredentialRepository);
            }

            @ConditionalOnMissingBean(name = "webAuthnController")
            @Bean
            @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
            public WebAuthnController webAuthnController(
                @Qualifier("webAuthnServer") final WebAuthnServer webAuthnServer) {
                return new WebAuthnController(webAuthnServer);
            }

        }

        @Configuration(value = "WebAuthnCryptoConfiguration", proxyBeanMethods = false)
        @EnableConfigurationProperties(CasConfigurationProperties.class)
        static class WebAuthnCryptoConfiguration {
            @ConditionalOnMissingBean(name = "webAuthnCredentialRegistrationCipherExecutor")
            @Bean
            @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
            public CipherExecutor webAuthnCredentialRegistrationCipherExecutor(
                final ConfigurableApplicationContext applicationContext,
                final CasConfigurationProperties casProperties) throws Exception {
                return BeanSupplier.of(CipherExecutor.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> {
                        val crypto = casProperties.getAuthn().getMfa().getWebAuthn().getCrypto();
                        if (crypto.isEnabled()) {
                            return CipherExecutorUtils.newStringCipherExecutor(crypto, WebAuthnCredentialRegistrationCipherExecutor.class);
                        }
                        LOGGER.trace("Web Authn credential registration records managed by CAS are not signed/encrypted.");
                        return CipherExecutor.noOp();
                    })
                    .otherwiseProxy()
                    .get();
            }
        }

        @Configuration(value = "WebAuthnSecurityConfiguration", proxyBeanMethods = false)
        @Order(CasWebSecurityConstants.SECURITY_CONFIGURATION_ORDER - 1)
        static class WebAuthnSecurityConfiguration {

            @Bean
            @ConditionalOnMissingBean(name = "webAuthnCsrfTokenRepository")
            @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
            public CsrfTokenRepository webAuthnCsrfTokenRepository() {
                return new CookieCsrfTokenRepository();
            }

            @Bean
            @ConditionalOnMissingBean(name = "webAuthnProtocolEndpointConfigurer")
            @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
            public CasWebSecurityConfigurer<HttpSecurity> webAuthnProtocolEndpointConfigurer(
                @Qualifier("webAuthnCsrfTokenRepository") final ObjectProvider<CsrfTokenRepository> webAuthnCsrfTokenRepository) {
                return new CasWebSecurityConfigurer<>() {
                    @Override
                    @CanIgnoreReturnValue
                    @SuppressWarnings("UnnecessaryMethodReference")
                    public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) throws Exception {
                        http.csrf(customizer -> webAuthnCsrfTokenRepository.ifAvailable(repository -> {
                            val pattern = new AntPathRequestMatcher(WebAuthnController.BASE_ENDPOINT_WEBAUTHN + "/**");
                            val delegate = new XorCsrfTokenRequestAttributeHandler();
                            delegate.setSecureRandom(RandomUtils.getNativeInstance());
                            customizer.requireCsrfProtectionMatcher(pattern)
                                .csrfTokenRequestHandler(delegate::handle)
                                .csrfTokenRepository(repository);

                        }));
                        http.authorizeHttpRequests(customizer -> {
                            val regEndpoints = new AntPathRequestMatcher(WebAuthnController.BASE_ENDPOINT_WEBAUTHN + WebAuthnController.WEBAUTHN_ENDPOINT_REGISTER + "/**");
                            val authEndpoints = new AntPathRequestMatcher(WebAuthnController.BASE_ENDPOINT_WEBAUTHN + WebAuthnController.WEBAUTHN_ENDPOINT_AUTHENTICATE + "/**");
                            customizer.requestMatchers(regEndpoints)
                                .access(new WebExpressionAuthorizationManager("hasRole('USER') and isAuthenticated()"));
                            customizer.requestMatchers(authEndpoints).permitAll();
                        });
                        return this;
                    }
                };
            }
        }
    }
}
