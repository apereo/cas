package org.apereo.cas.pm.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.DefaultPasswordValidationService;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.impl.GroovyResourcePasswordManagementService;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementService;
import org.apereo.cas.pm.impl.NoOpPasswordManagementService;
import org.apereo.cas.pm.impl.history.AmnesiacPasswordHistoryService;
import org.apereo.cas.pm.impl.history.GroovyPasswordHistoryService;
import org.apereo.cas.pm.impl.history.InMemoryPasswordHistoryService;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.BooleanAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.FirstParameterAuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.ShortenedReturnValueAsStringAuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.SpringWebflowActionExecutionAuditablePrincipalResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link PasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "passwordManagementConfiguration", proxyBeanMethods = false)
public class PasswordManagementConfiguration {
    
    @Configuration(value = "PasswordManagementValidationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementValidationConfiguration {
        @ConditionalOnMissingBean(name = "passwordValidationService")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public PasswordValidationService passwordValidationService(final CasConfigurationProperties casProperties,
                                                                   @Qualifier("passwordHistoryService")
                                                                   final PasswordHistoryService passwordHistoryService) {
            val policyPattern = casProperties.getAuthn().getPm().getCore().getPasswordPolicyPattern();
            return new DefaultPasswordValidationService(policyPattern, passwordHistoryService);
        }
    }
    
    @Configuration(value = "PasswordManagementHistoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementHistoryConfiguration {
        @ConditionalOnMissingBean(name = "passwordHistoryService")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public PasswordHistoryService passwordHistoryService(final CasConfigurationProperties casProperties) {
            val pm = casProperties.getAuthn().getPm();
            val history = pm.getHistory();
            if (pm.getCore().isEnabled() && history.getCore().isEnabled()) {
                if (history.getGroovy().getLocation() != null) {
                    return new GroovyPasswordHistoryService(history.getGroovy().getLocation());
                }
                return new InMemoryPasswordHistoryService();
            }
            return new AmnesiacPasswordHistoryService();
        }
    }

    @Configuration(value = "PasswordManagementAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementAuditConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementReturnValueResourceResolver")
        @Bean
        public AuditResourceResolver passwordManagementReturnValueResourceResolver() {
            return new ShortenedReturnValueAsStringAuditResourceResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordManagementAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer passwordManagementAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.CHANGE_PASSWORD_ACTION_RESOLVER,
                    new BooleanAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.CHANGE_PASSWORD_RESOURCE_RESOLVER, new FirstParameterAuditResourceResolver());
                plan.registerAuditActionResolver(AuditActionResolvers.REQUEST_CHANGE_PASSWORD_ACTION_RESOLVER, new DefaultAuditActionResolver());
                plan.registerAuditResourceResolver(AuditResourceResolvers.REQUEST_CHANGE_PASSWORD_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditPrincipalResolver(AuditPrincipalResolvers.REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER,
                    new SpringWebflowActionExecutionAuditablePrincipalResolver("username"));
            };
        }
    }

    @Configuration(value = "PasswordManagementCipherConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementCipherConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public CipherExecutor passwordManagementCipherExecutor(final CasConfigurationProperties casProperties) {
            val pm = casProperties.getAuthn().getPm();
            val crypto = pm.getReset().getCrypto();
            if (pm.getCore().isEnabled() && crypto.isEnabled()) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, PasswordResetTokenCipherExecutor.class);
            }
            return CipherExecutor.noOp();
        }
    }
    
    @Configuration(value = "PasswordManagementCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementCoreConfiguration {
        
        @ConditionalOnMissingBean(name = PasswordManagementService.DEFAULT_BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public PasswordManagementService passwordChangeService(
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordManagementCipherExecutor")
            final CipherExecutor passwordManagementCipherExecutor,
            @Qualifier("passwordHistoryService")
            final PasswordHistoryService passwordHistoryService) {
            val pm = casProperties.getAuthn().getPm();
            if (pm.getCore().isEnabled()) {
                val location = pm.getJson().getLocation();
                if (location != null) {
                    LOGGER.debug("Configuring password management based on JSON resource [{}]", location);
                    return new JsonResourcePasswordManagementService(passwordManagementCipherExecutor,
                        casProperties.getServer().getPrefix(), casProperties.getAuthn().getPm(), location,
                        passwordHistoryService);
                }
                val groovyScript = pm.getGroovy().getLocation();
                if (groovyScript != null) {
                    LOGGER.debug("Configuring password management based on Groovy resource [{}]", groovyScript);
                    return new GroovyResourcePasswordManagementService(passwordManagementCipherExecutor,
                        casProperties.getServer().getPrefix(), casProperties.getAuthn().getPm(),
                        groovyScript,
                        passwordHistoryService);
                }
                LOGGER.warn("No storage service is configured to handle the account update and password service operations. "
                            + "Password management functionality will have no effect and will be disabled until a storage service is configured. "
                            + "To explicitly disable the password management, add 'cas.authn.pm.core.enabled=false' to the CAS configuration");
            } else {
                LOGGER.debug("Password management is disabled. To enable the password management functionality, "
                             + "add 'cas.authn.pm.core.enabled=true' to the CAS configuration and then configure storage options for account updates");
            }
            return new NoOpPasswordManagementService(passwordManagementCipherExecutor, casProperties.getServer().getPrefix(), casProperties.getAuthn().getPm());
        }

        @Autowired
        @Bean
        public InitializingBean afterPropertiesSet(final CasConfigurationProperties casProperties,
                                                   @Qualifier("communicationsManager")
                                                   final CommunicationsManager communicationsManager) {
            return () -> {
                val pm = casProperties.getAuthn().getPm();
                if (pm.getCore().isEnabled()) {
                    communicationsManager.validate();
                }
            };
        }
    }
}
