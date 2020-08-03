package org.apereo.cas.trusted.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.util.cipher.CookieDeviceFingerprintComponentCipherExecutor;
import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.CookieDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.DefaultDeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.fingerprint.GeoLocationDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.UserAgentDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.support.TrustedDeviceCookieRetrievingCookieGenerator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.mgmr.EncryptedCookieValueManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_COOKIE_CIPHER_EXECUTOR;
import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_COOKIE_GENERATOR;
import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_COOKIE_RANDOM_STRING_GENERATOR;
import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_STRATEGY;

/**
 * Configuration for {@link DefaultDeviceFingerprintStrategy}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Slf4j
@Configuration("multifactorAuthnTrustedDeviceFingerprintConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MultifactorAuthnTrustedDeviceFingerprintConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("geoLocationService")
    private ObjectProvider<GeoLocationService> geoLocationService;

    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.client-ip", name = "enabled", havingValue = "true")
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "deviceFingerprintClientIpComponentExtractor")
    public DeviceFingerprintComponentExtractor deviceFingerprintClientIpComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getClientIp();
        val component = new ClientIpDeviceFingerprintComponentExtractor();
        component.setOrder(properties.getOrder());
        return component;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "deviceFingerprintGeoLocationComponentExtractor")
    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.geolocation", name = "enabled", havingValue = "true")
    public DeviceFingerprintComponentExtractor deviceFingerprintGeoLocationComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getGeolocation();
        val component = new GeoLocationDeviceFingerprintComponentExtractor(geoLocationService.getIfAvailable());
        component.setOrder(properties.getOrder());
        return component;
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.cookie", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "deviceFingerprintCookieComponentExtractor")
    public DeviceFingerprintComponentExtractor deviceFingerprintCookieComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
        val component = new CookieDeviceFingerprintComponentExtractor(
            deviceFingerprintCookieGenerator(),
            deviceFingerprintCookieRandomStringGenerator());
        component.setOrder(properties.getOrder());
        return component;
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.cookie", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_COOKIE_GENERATOR)
    @Bean(BEAN_DEVICE_FINGERPRINT_COOKIE_GENERATOR)
    @RefreshScope
    public CasCookieBuilder deviceFingerprintCookieGenerator() {
        val cookie = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
        return new TrustedDeviceCookieRetrievingCookieGenerator(
            CookieUtils.buildCookieGenerationContext(cookie),
            deviceFingerprintCookieValueManager()
        );
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.user-agent", name = "enabled", havingValue = "true")
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "deviceFingerprintUserAgentComponentExtractor")
    public DeviceFingerprintComponentExtractor deviceFingerprintUserAgentComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getUserAgent();
        val component = new UserAgentDeviceFingerprintComponentExtractor();
        component.setOrder(properties.getOrder());
        return component;
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_STRATEGY)
    @Bean(BEAN_DEVICE_FINGERPRINT_STRATEGY)
    @RefreshScope
    public DeviceFingerprintStrategy deviceFingerprintStrategy(final List<DeviceFingerprintComponentExtractor> extractors) {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint();
        return new DefaultDeviceFingerprintStrategy(extractors, properties.getComponentSeparator());
    }


    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_COOKIE_RANDOM_STRING_GENERATOR)
    @Bean(BEAN_DEVICE_FINGERPRINT_COOKIE_RANDOM_STRING_GENERATOR)
    public RandomStringGenerator deviceFingerprintCookieRandomStringGenerator() {
        return new Base64RandomStringGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(name = "deviceFingerprintCookieValueManager")
    public CookieValueManager deviceFingerprintCookieValueManager() {
        return new EncryptedCookieValueManager(deviceFingerprintCookieCipherExecutor());
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_COOKIE_CIPHER_EXECUTOR)
    @Bean(BEAN_DEVICE_FINGERPRINT_COOKIE_CIPHER_EXECUTOR)
    @RefreshScope
    public CipherExecutor deviceFingerprintCookieCipherExecutor() {
        val crypto = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie().getCrypto();

        var enabled = crypto.isEnabled();
        if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
            LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration, yet "
                + "signing/encryption keys are defined for operations. CAS will proceed to enable the cookie "
                + "encryption/signing functionality.");
            enabled = true;
        }

        if (enabled) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, CookieDeviceFingerprintComponentCipherExecutor.class);
        }

        return CipherExecutor.noOp();
    }
}
