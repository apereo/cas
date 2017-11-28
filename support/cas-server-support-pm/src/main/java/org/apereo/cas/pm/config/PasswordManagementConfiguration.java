package org.apereo.cas.pm.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementService;
import org.apereo.cas.pm.impl.NoOpPasswordManagementService;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.io.CommunicationsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import javax.annotation.PostConstruct;

/**
 * This is {@link PasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("passwordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordManagementConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordManagementConfiguration.class);
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @ConditionalOnMissingBean(name = "passwordManagementCipherExecutor")
    @RefreshScope
    @Bean
    public CipherExecutor passwordManagementCipherExecutor() {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        final EncryptionJwtSigningJwtCryptographyProperties crypto = pm.getReset().getCrypto();
        if (pm.isEnabled() && crypto.isEnabled()) {
            return new PasswordResetTokenCipherExecutor(
                    crypto.getEncryption().getKey(),
                    crypto.getSigning().getKey(),
                    crypto.getAlg());
        }
        return NoOpCipherExecutor.getInstance();
    }

    @ConditionalOnMissingBean(name = "passwordValidationService")
    @RefreshScope
    @Bean
    public PasswordValidationService passwordValidationService() {
        final String policyPattern = casProperties.getAuthn().getPm().getPolicyPattern();
        return (credential, bean) -> {
            return StringUtils.hasText(bean.getPassword())
                && bean.getPassword().equals(bean.getConfirmedPassword())
                && bean.getPassword().matches(policyPattern);
        };
    }
    
    @ConditionalOnMissingBean(name = "passwordChangeService")
    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            final Resource location = pm.getJson().getLocation();
            if (location != null) {
                LOGGER.debug("Configuring password management based on JSON resource [{}]", location);
                return new JsonResourcePasswordManagementService(passwordManagementCipherExecutor(),
                        casProperties.getServer().getPrefix(),
                        casProperties.getAuthn().getPm(), location);
            }
            LOGGER.warn("No storage service (LDAP, Database, etc) is configured to handle the account update and password service operations. "
                    + "Password management functionality will have no effect and will be disabled until a storage service is configured. "
                    + "To explicitly disable the password management functionality, add 'cas.authn.pm.enabled=false' to the CAS configuration");
        } else {
            LOGGER.debug("Password management is disabled. To enable the password management functionality, "
                    + "add 'cas.authn.pm.enabled=true' to the CAS configuration and then configure storage options for account updates");
        }
        return new NoOpPasswordManagementService(passwordManagementCipherExecutor(),
                casProperties.getServer().getPrefix(),
                casProperties.getAuthn().getPm());
    }

    @PostConstruct
    public void init() {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            if (!communicationsManager.isMailSenderDefined()) {
                LOGGER.warn("CAS is unable to send password-reset emails given no settings are defined to account for email servers, etc");
            }
            if (!communicationsManager.isSmsSenderDefined()) {
                LOGGER.warn("CAS is unable to send password-reset sms messages given no settings are defined to account for sms providers, etc");
            }
        }
    }
}



