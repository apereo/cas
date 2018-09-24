package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.DefaultPasswordValidationService;
import org.apereo.cas.pm.GroovyResourcePasswordManagementService;
import org.apereo.cas.pm.JsonResourcePasswordManagementService;
import org.apereo.cas.pm.NoOpPasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.util.io.CommunicationsManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.BooleanAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.FirstParameterAuditResourceResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link PasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("passwordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class PasswordManagementConfiguration implements AuditTrailRecordResolutionPlanConfigurer, InitializingBean {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @ConditionalOnMissingBean(name = "passwordManagementCipherExecutor")
    @RefreshScope
    @Bean
    public CipherExecutor passwordManagementCipherExecutor() {
        val pm = casProperties.getAuthn().getPm();
        val crypto = pm.getReset().getCrypto();
        if (pm.isEnabled() && crypto.isEnabled()) {
            return new PasswordResetTokenCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        }
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "passwordValidationService")
    @RefreshScope
    @Bean
    public PasswordValidationService passwordValidationService() {
        val policyPattern = casProperties.getAuthn().getPm().getPolicyPattern();
        return new DefaultPasswordValidationService(policyPattern);
    }

    @ConditionalOnMissingBean(name = "passwordChangeService")
    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        val pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            val location = pm.getJson().getLocation();
            if (location != null) {
                LOGGER.debug("Configuring password management based on JSON resource [{}]", location);
                return new JsonResourcePasswordManagementService(passwordManagementCipherExecutor(),
                    casProperties.getServer().getPrefix(),
                    casProperties.getAuthn().getPm(), location);
            }

            val groovyScript = pm.getGroovy().getLocation();
            if (groovyScript != null) {
                LOGGER.debug("Configuring password management based on Groovy resource [{}]", groovyScript);
                return new GroovyResourcePasswordManagementService(passwordManagementCipherExecutor(),
                    casProperties.getServer().getPrefix(),
                    casProperties.getAuthn().getPm(), groovyScript);
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

    @Override
    public void afterPropertiesSet() {
        val pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            communicationsManager.getIfAvailable().validate();
        }
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditActionResolver("CHANGE_PASSWORD_ACTION_RESOLVER",
            new BooleanAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
        plan.registerAuditResourceResolver("CHANGE_PASSWORD_RESOURCE_RESOLVER",
            new FirstParameterAuditResourceResolver());
    }
}



