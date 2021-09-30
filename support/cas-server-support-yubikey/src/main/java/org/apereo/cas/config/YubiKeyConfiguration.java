package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubikeyAccountCipherExecutor;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration(value = "yubikeyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class YubiKeyConfiguration {

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "transactionManagerYubiKey")
    public PlatformTransactionManager transactionManagerYubiKey() {
        return new PseudoPlatformTransactionManager();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyAccountCipherExecutor")
    @Autowired
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
