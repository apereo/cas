package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubikeyAccountCipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey)
@Configuration(value = "YubiKeyConfiguration", proxyBeanMethods = false)
class YubiKeyConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "transactionManagerYubiKey")
    public PlatformTransactionManager transactionManagerYubiKey() {
        return new PseudoTransactionManager();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyAccountCipherExecutor")
    public CipherExecutor yubikeyAccountCipherExecutor(final CasConfigurationProperties casProperties) {
        val crypto = casProperties.getAuthn().getMfa().getYubikey().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, YubikeyAccountCipherExecutor.class);
        }
        LOGGER.info("YubiKey account encryption/signing is turned off and "
                    + "MAY NOT be safe in a production environment. "
                    + "Consider using other choices to handle encryption, signing and verification of " + "YubiKey accounts for MFA");
        return CipherExecutor.noOp();
    }
}
