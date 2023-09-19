package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.trusted.util.cipher.CookieDeviceFingerprintComponentCipherExecutor;
import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintComponentManager;
import org.apereo.cas.trusted.web.flow.fingerprint.CookieDeviceFingerprintComponentManager;
import org.apereo.cas.trusted.web.flow.fingerprint.DefaultDeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintComponentManager;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.fingerprint.GeoLocationDeviceFingerprintComponentManager;
import org.apereo.cas.trusted.web.flow.fingerprint.UserAgentDeviceFingerprintComponentManager;
import org.apereo.cas.trusted.web.support.TrustedDeviceCookieRetrievingCookieGenerator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import org.apereo.cas.web.support.mgmr.EncryptedCookieValueManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration for {@link DefaultDeviceFingerprintStrategy}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices)
@AutoConfiguration
public class MultifactorAuthnTrustedDeviceFingerprintConfiguration {

    @Configuration(value = "MultifactorAuthnTrustedDeviceFingerprintComponentConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustedDeviceFingerprintComponentConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "deviceFingerprintUserAgentComponentExtractor")
        public DeviceFingerprintComponentManager deviceFingerprintUserAgentComponentExtractor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DeviceFingerprintComponentManager.class)
                .when(BeanCondition.on("cas.authn.mfa.trusted.device-fingerprint.user-agent.enabled").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getUserAgent();
                    val component = new UserAgentDeviceFingerprintComponentManager();
                    component.setOrder(properties.getOrder());
                    return component;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "deviceFingerprintClientIpComponentExtractor")
        public DeviceFingerprintComponentManager deviceFingerprintClientIpComponentExtractor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DeviceFingerprintComponentManager.class)
                .when(BeanCondition.on("cas.authn.mfa.trusted.device-fingerprint.client-ip.enabled").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getClientIp();
                    val component = new ClientIpDeviceFingerprintComponentManager();
                    component.setOrder(properties.getOrder());
                    return component;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "deviceFingerprintCookieComponentExtractor")
        public DeviceFingerprintComponentManager deviceFingerprintCookieComponentExtractor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("deviceFingerprintCookieGenerator")
            final CasCookieBuilder deviceFingerprintCookieGenerator,
            @Qualifier("deviceFingerprintCookieRandomStringGenerator")
            final RandomStringGenerator deviceFingerprintCookieRandomStringGenerator) {
            return BeanSupplier.of(DeviceFingerprintComponentManager.class)
                .when(BeanCondition.on("cas.authn.mfa.trusted.device-fingerprint.cookie.enabled").isTrue().evenIfMissing().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
                    val component = new CookieDeviceFingerprintComponentManager(deviceFingerprintCookieGenerator, deviceFingerprintCookieRandomStringGenerator);
                    component.setOrder(properties.getOrder());
                    return component;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustedDeviceFingerprintStrategyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustedDeviceFingerprintStrategyConfiguration {

        @ConditionalOnMissingBean(name = DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
        @Bean(DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DeviceFingerprintStrategy deviceFingerprintStrategy(final List<DeviceFingerprintComponentManager> extractors,
                                                                   final CasConfigurationProperties casProperties) {
            val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint();
            val activeExtractors = extractors.stream().filter(BeanSupplier::isNotProxy).collect(Collectors.toList());
            return new DefaultDeviceFingerprintStrategy(activeExtractors, properties.getComponentSeparator());
        }

    }

    @Configuration(value = "MultifactorAuthnTrustedDeviceFingerprintCookieConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustedDeviceFingerprintCookieConfiguration {
        @ConditionalOnMissingBean(name = "deviceFingerprintCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder deviceFingerprintCookieGenerator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("deviceFingerprintCookieValueManager")
            final CookieValueManager deviceFingerprintCookieValueManager) {
            return BeanSupplier.of(CasCookieBuilder.class)
                .when(BeanCondition.on("cas.authn.mfa.trusted.device-fingerprint.cookie.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cookie = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
                    return new TrustedDeviceCookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(cookie),
                        deviceFingerprintCookieValueManager);
                })
                .otherwiseProxy()
                .get();
        }


        @ConditionalOnMissingBean(name = "deviceFingerprintCookieRandomStringGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RandomStringGenerator deviceFingerprintCookieRandomStringGenerator() {
            return new Base64RandomStringGenerator();
        }

        @Bean
        @ConditionalOnMissingBean(name = "deviceFingerprintCookieValueManager")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CookieValueManager deviceFingerprintCookieValueManager(
            @Qualifier("deviceFingerprintCookieCipherExecutor")
            final CipherExecutor deviceFingerprintCookieCipherExecutor) {
            return new EncryptedCookieValueManager(deviceFingerprintCookieCipherExecutor,
                DefaultCookieSameSitePolicy.INSTANCE);
        }

        @ConditionalOnMissingBean(name = "deviceFingerprintCookieCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CipherExecutor deviceFingerprintCookieCipherExecutor(final CasConfigurationProperties casProperties) {
            val cookie = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
            val crypto = cookie.getCrypto();
            var enabled = crypto.isEnabled();
            if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
                LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration for cookie [{}], yet "
                            + "signing/encryption keys are defined for operations. CAS will proceed to enable the cookie "
                            + "encryption/signing functionality.", cookie.getName());
                enabled = true;
            }
            if (enabled) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, CookieDeviceFingerprintComponentCipherExecutor.class);
            }
            return CipherExecutor.noOp();
        }

    }

    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnBean(name = GeoLocationService.BEAN_NAME)
    @Configuration(value = "MultifactorAuthnTrustedDeviceGeoLocationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustedDeviceGeoLocationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "deviceFingerprintGeoLocationComponentExtractor")
        public DeviceFingerprintComponentManager deviceFingerprintGeoLocationComponentExtractor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService) {
            return BeanSupplier.of(DeviceFingerprintComponentManager.class)
                .when(BeanCondition.on("cas.authn.mfa.trusted.device-fingerprint.geolocation.enabled").isTrue()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getGeolocation();
                    val component = new GeoLocationDeviceFingerprintComponentManager(geoLocationService.getObject());
                    component.setOrder(properties.getOrder());
                    return component;
                })
                .otherwiseProxy()
                .get();
        }
    }
}
