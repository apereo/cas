package org.apereo.cas.trusted.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.util.cipher.CookieDeviceFingerprintComponentCipherExecutor;
import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.CookieDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.DefaultDeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.trusted.web.flow.fingerprint.UserAgentDeviceFingerprintComponentExtractor;
import org.apereo.cas.trusted.web.support.TrustedDeviceCookieRetrievingCookieGenerator;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.CookieValueManager;
import org.apereo.cas.web.support.EncryptedCookieValueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Bean
    @RefreshScope
    public DeviceFingerprintComponentExtractor deviceFingerprintClientIpComponent() {
        final var properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getClientIp();
        if (properties.isEnabled()) {
            final var component = new ClientIpDeviceFingerprintComponentExtractor();
            component.setOrder(properties.getOrder());
            return component;
        }

        LOGGER.info("The client ip is not being included when creating MFA Trusted Device Fingerprints");
        return DeviceFingerprintComponentExtractor.noOp();
    }

    @Bean
    @RefreshScope
    public DeviceFingerprintComponentExtractor deviceFingerprintCookieComponent() {
        final var properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
        if (properties.isEnabled()) {
            final var component = new CookieDeviceFingerprintComponentExtractor(
                deviceFingerprintCookieGenerator(), deviceFingerprintCookieRandomStringGenerator());
            component.setOrder(properties.getOrder());
            return component;
        }

        LOGGER.info("A persistent cookie is not being generated when creating MFA Trusted Device Fingerprints");
        return DeviceFingerprintComponentExtractor.noOp();
    }

    @Bean
    @RefreshScope
    public DeviceFingerprintComponentExtractor deviceFingerprintUserAgentComponent() {
        final var properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getUserAgent();
        if (properties.isEnabled()) {
            final var component = new UserAgentDeviceFingerprintComponentExtractor();
            component.setOrder(properties.getOrder());
            return component;
        }

        LOGGER.info("User-Agent is not being included when creating MFA Trusted Device Fingerprints");
        return DeviceFingerprintComponentExtractor.noOp();
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_STRATEGY)
    @Bean(BEAN_DEVICE_FINGERPRINT_STRATEGY)
    @RefreshScope
    public DeviceFingerprintStrategy deviceFingerprintStrategy(final List<DeviceFingerprintComponentExtractor> extractors) {
        final var properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint();
        return new DefaultDeviceFingerprintStrategy(extractors, properties.getComponentSeparator());
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_COOKIE_GENERATOR)
    @Bean(BEAN_DEVICE_FINGERPRINT_COOKIE_GENERATOR)
    @RefreshScope
    public CookieRetrievingCookieGenerator deviceFingerprintCookieGenerator() {
        final var cookie = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
        return new TrustedDeviceCookieRetrievingCookieGenerator(
            cookie.getName(),
            cookie.getPath(),
            cookie.getMaxAge(),
            cookie.isSecure(),
            cookie.getDomain(),
            cookie.isHttpOnly(),
            deviceFingerprintCookieValueManager()
        );
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_COOKIE_RANDOM_STRING_GENERATOR)
    @Bean(BEAN_DEVICE_FINGERPRINT_COOKIE_RANDOM_STRING_GENERATOR)
    public RandomStringGenerator deviceFingerprintCookieRandomStringGenerator() {
        return new Base64RandomStringGenerator();
    }

    @Bean
    public CookieValueManager deviceFingerprintCookieValueManager() {
        return new EncryptedCookieValueManager(deviceFingerprintCookieCipherExecutor());
    }

    @ConditionalOnMissingBean(name = BEAN_DEVICE_FINGERPRINT_COOKIE_CIPHER_EXECUTOR)
    @Bean(BEAN_DEVICE_FINGERPRINT_COOKIE_CIPHER_EXECUTOR)
    @RefreshScope
    public CipherExecutor deviceFingerprintCookieCipherExecutor() {
        final var crypto =
            casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie().getCrypto();

        var enabled = crypto.isEnabled();
        if (!enabled && (StringUtils.isNotBlank(crypto.getEncryption().getKey())) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
            LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration, yet "
                + "signing/encryption keys are defined for operations. CAS will proceed to enable the cookie "
                + "encryption/signing functionality.");
            enabled = true;
        }

        if (enabled) {
            return new CookieDeviceFingerprintComponentCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        }

        return CipherExecutor.noOp();
    }
}
