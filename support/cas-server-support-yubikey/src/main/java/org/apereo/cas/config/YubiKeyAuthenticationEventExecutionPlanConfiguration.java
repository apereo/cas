package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.AcceptAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.DefaultYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.DenyAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyCredential;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticatorDeviceManager;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.JsonYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.PermissiveYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.RestfulYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.YubiKeyAccountRegistryEndpoint;
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
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.yubico.client.v2.YubicoClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is {@link YubiKeyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey)
@Configuration(value = "YubiKeyAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class YubiKeyAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "YubiMultifactorAuthenticationAccountsConfiguration", proxyBeanMethods = false)
    static class YubiMultifactorAuthenticationAccountsConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "yubicoClient")
        public YubicoClient yubicoClient(final CasConfigurationProperties casProperties) {
            val yubi = casProperties.getAuthn().getMfa().getYubikey();
            val client = YubicoClient.getClient(yubi.getClientId(), yubi.getSecretKey());
            if (!yubi.getApiUrls().isEmpty()) {
                val urls = yubi.getApiUrls().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
                client.setWsapiUrls(urls);
            }
            return client;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubiKeyAccountValidator")
        public YubiKeyAccountValidator yubiKeyAccountValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier("yubicoClient")
            final YubicoClient yubicoClient) {
            val yubi = casProperties.getAuthn().getMfa().getYubikey();
            return switch (yubi.getValidator()) {
                case SKIP -> new AcceptAllYubiKeyAccountValidator();
                case REJECT -> new DenyAllYubiKeyAccountValidator();
                case VERIFY -> new DefaultYubiKeyAccountValidator(yubicoClient);
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubiKeyAccountRegistry")
        public YubiKeyAccountRegistry yubiKeyAccountRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier("yubiKeyAccountValidator")
            final YubiKeyAccountValidator yubiKeyAccountValidator,
            @Qualifier("yubicoClient")
            final YubicoClient yubicoClient,
            @Qualifier("yubikeyAccountCipherExecutor")
            final CipherExecutor yubikeyAccountCipherExecutor) throws Exception {
            val yubi = casProperties.getAuthn().getMfa().getYubikey();
            if (yubi.getJson().getLocation() != null) {
                LOGGER.debug("Using JSON resource [{}] as the YubiKey account registry", yubi.getJson().getLocation());
                val registry = new JsonYubiKeyAccountRegistry(yubi.getJson().getLocation(),
                    yubi.getJson().isWatchResource(), yubiKeyAccountValidator);
                registry.setCipherExecutor(yubikeyAccountCipherExecutor);
                return registry;
            }
            if (StringUtils.isNotBlank(yubi.getRest().getUrl())) {
                LOGGER.debug("Using REST API resource [{}] as the YubiKey account registry", yubi.getRest().getUrl());
                val registry = new RestfulYubiKeyAccountRegistry(yubi.getRest(), yubiKeyAccountValidator);
                registry.setCipherExecutor(yubikeyAccountCipherExecutor);
                return registry;
            }
            if (yubi.getAllowedDevices() != null && !yubi.getAllowedDevices().isEmpty()) {
                LOGGER.debug("Using statically-defined devices for [{}] as the YubiKey account registry",
                    yubi.getAllowedDevices().keySet());
                val map = (Map<String, YubiKeyAccount>) yubi.getAllowedDevices()
                    .entrySet()
                    .stream()
                    .map(entry -> YubiKeyAccount.builder().id(System.currentTimeMillis()).username(entry.getKey())
                        .devices(CollectionUtils.wrapList(YubiKeyRegisteredDevice.builder()
                            .publicId(entry.getValue())
                            .name(UUID.randomUUID().toString())
                            .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                            .id(System.currentTimeMillis())
                            .build()))
                        .build())
                    .collect(Collectors.toMap(YubiKeyAccount::getUsername, Function.identity()));
                val registry = new PermissiveYubiKeyAccountRegistry(map, yubiKeyAccountValidator);
                registry.setCipherExecutor(CipherExecutor.noOpOfSerializableToString());
                return registry;
            }
            LOGGER.warn("All credentials are considered eligible for YubiKey authentication. "
                    + "Consider providing an account registry implementation via [{}]",
                YubiKeyAccountRegistry.class.getName());
            val registry = new OpenYubiKeyAccountRegistry(yubiKeyAccountValidator);
            registry.setCipherExecutor(yubikeyAccountCipherExecutor);
            return registry;
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public YubiKeyAccountRegistryEndpoint yubiKeyAccountRegistryEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("yubiKeyAccountRegistry")
            final ObjectProvider<YubiKeyAccountRegistry> yubiKeyAccountRegistry) {
            return new YubiKeyAccountRegistryEndpoint(casProperties, applicationContext, yubiKeyAccountRegistry);
        }

        @ConditionalOnMissingBean(name = "yubiKeyMultifactorAuthenticatorDeviceManager")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationDeviceManager yubiKeyMultifactorAuthenticatorDeviceManager(
            @Qualifier("yubikeyMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> yubikeyMultifactorAuthenticationProvider,
            @Qualifier("yubiKeyAccountRegistry")
            final YubiKeyAccountRegistry yubiKeyAccountRegistry) {
            return new YubiKeyMultifactorAuthenticatorDeviceManager(yubiKeyAccountRegistry,
                yubikeyMultifactorAuthenticationProvider);
        }
    }

    @Configuration(value = "YubiMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
    static class YubiMultifactorAuthenticationConfiguration {
        @ConditionalOnMissingBean(name = "yubikeyPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory yubikeyPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubikeyAuthenticationHandler")
        public AuthenticationHandler yubikeyAuthenticationHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("yubikeyPrincipalFactory")
            final PrincipalFactory yubikeyPrincipalFactory,
            @Qualifier("yubicoClient")
            final YubicoClient yubicoClient,
            @Qualifier("yubiKeyAccountRegistry")
            final YubiKeyAccountRegistry yubiKeyAccountRegistry,
            @Qualifier("yubikeyMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val yubi = casProperties.getAuthn().getMfa().getYubikey();
            return new YubiKeyAuthenticationHandler(yubi.getName(),
                yubikeyPrincipalFactory, yubicoClient, yubiKeyAccountRegistry,
                yubi.getOrder(), multifactorAuthenticationProvider);
        }


        @ConditionalOnMissingBean(name = "yubikeyMultifactorAuthenticationProvider")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationProvider yubikeyMultifactorAuthenticationProvider(
            @Qualifier("yubiKeyMultifactorAuthenticatorDeviceManager")
            final MultifactorAuthenticationDeviceManager yubiKeyMultifactorAuthenticatorDeviceManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("yubicoClient")
            final YubicoClient yubicoClient,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            @Qualifier("yubikeyBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator yubikeyBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            val yubi = casProperties.getAuthn().getMfa().getYubikey();
            val provider = new YubiKeyMultifactorAuthenticationProvider(yubicoClient, httpClient);
            provider.setBypassEvaluator(yubikeyBypassEvaluator);
            provider.setFailureMode(yubi.getFailureMode());
            provider.setFailureModeEvaluator(failureModeEvaluator);
            provider.setOrder(yubi.getRank());
            provider.setId(yubi.getId());
            provider.setDeviceManager(yubiKeyMultifactorAuthenticatorDeviceManager);
            return provider;
        }


        @ConditionalOnMissingBean(name = "yubikeyAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer yubikeyAuthenticationEventExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("yubikeyMultifactorProviderAuthenticationMetadataPopulator")
            final AuthenticationMetaDataPopulator yubikeyMultifactorProviderAuthenticationMetadataPopulator,
            @Qualifier("yubikeyAuthenticationHandler")
            final AuthenticationHandler yubikeyAuthenticationHandler,
            @Qualifier("yubikeyAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator) {
            return plan -> {
                val yubi = casProperties.getAuthn().getMfa().getYubikey();
                if (yubi.getClientId() > 0 && StringUtils.isNotBlank(yubi.getSecretKey())) {
                    plan.registerAuthenticationHandler(yubikeyAuthenticationHandler);
                    plan.registerAuthenticationMetadataPopulator(yubikeyAuthenticationMetaDataPopulator);
                    plan.registerAuthenticationMetadataPopulator(yubikeyMultifactorProviderAuthenticationMetadataPopulator);
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(YubiKeyCredential.class));
                }
            };
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubikeyMultifactorProviderAuthenticationMetadataPopulator")
        public AuthenticationMetaDataPopulator yubikeyMultifactorProviderAuthenticationMetadataPopulator(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("yubikeyMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> yubikeyMultifactorAuthenticationProvider) {
            val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
                yubikeyMultifactorAuthenticationProvider, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubikeyAuthenticationMetaDataPopulator")
        public AuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties,
            @Qualifier("yubikeyAuthenticationHandler")
            final AuthenticationHandler yubikeyAuthenticationHandler,
            @Qualifier("yubikeyMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider yubikeyMultifactorAuthenticationProvider) {
            val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new AuthenticationContextAttributeMetaDataPopulator(authenticationContextAttribute,
                yubikeyAuthenticationHandler, yubikeyMultifactorAuthenticationProvider.getId());
        }

    }
}
