package org.apereo.cas.trusted.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
    public DeviceFingerprintComponentManager deviceFingerprintClientIpComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getClientIp();
        val component = new ClientIpDeviceFingerprintComponentManager();
        component.setOrder(properties.getOrder());
        return component;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "deviceFingerprintGeoLocationComponentExtractor")
    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.geolocation", name = "enabled", havingValue = "true")
    public DeviceFingerprintComponentManager deviceFingerprintGeoLocationComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getGeolocation();
        val component = new GeoLocationDeviceFingerprintComponentManager(geoLocationService.getIfAvailable());
        component.setOrder(properties.getOrder());
        return component;
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.cookie", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "deviceFingerprintCookieComponentExtractor")
    public DeviceFingerprintComponentManager deviceFingerprintCookieComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getCookie();
        val component = new CookieDeviceFingerprintComponentManager(
            deviceFingerprintCookieGenerator(),
            deviceFingerprintCookieRandomStringGenerator());
        component.setOrder(properties.getOrder());
        return component;
    }

    @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.device-fingerprint.cookie", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "deviceFingerprintCookieGenerator")
    @Bean
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
    public DeviceFingerprintComponentManager deviceFingerprintUserAgentComponentExtractor() {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint().getUserAgent();
        val component = new UserAgentDeviceFingerprintComponentManager();
        component.setOrder(properties.getOrder());
        return component;
    }

    @ConditionalOnMissingBean(name = DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
    @Bean(DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
    @RefreshScope
    public DeviceFingerprintStrategy deviceFingerprintStrategy(final List<DeviceFingerprintComponentManager> extractors) {
        val properties = casProperties.getAuthn().getMfa().getTrusted().getDeviceFingerprint();
        return new DefaultDeviceFingerprintStrategy(extractors, properties.getComponentSeparator());
    }


    @ConditionalOnMissingBean(name = "deviceFingerprintCookieRandomStringGenerator")
    @Bean
    public RandomStringGenerator deviceFingerprintCookieRandomStringGenerator() {
        return new Base64RandomStringGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(name = "deviceFingerprintCookieValueManager")
    public CookieValueManager deviceFingerprintCookieValueManager() {
        return new EncryptedCookieValueManager(deviceFingerprintCookieCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "deviceFingerprintCookieCipherExecutor")
    @Bean
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
