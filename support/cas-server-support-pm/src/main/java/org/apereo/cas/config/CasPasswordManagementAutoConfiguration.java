package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.impl.DefaultPasswordResetUrlBuilder;
import org.apereo.cas.pm.impl.DefaultPasswordValidationService;
import org.apereo.cas.pm.impl.GroovyResourcePasswordManagementService;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementService;
import org.apereo.cas.pm.impl.NoOpPasswordManagementService;
import org.apereo.cas.pm.impl.RestfulPasswordSynchronizationAuthenticationPostProcessor;
import org.apereo.cas.pm.impl.history.AmnesiacPasswordHistoryService;
import org.apereo.cas.pm.impl.history.GroovyPasswordHistoryService;
import org.apereo.cas.pm.impl.history.InMemoryPasswordHistoryService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.CasApplicationReadyListener;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.BooleanAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.FirstParameterAuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.ShortenedReturnValueAsStringAuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.SpringWebflowActionExecutionAuditablePrincipalResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasPasswordManagementAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement)
@AutoConfiguration
public class CasPasswordManagementAutoConfiguration {

    @Configuration(value = "PasswordManagementValidationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementValidationConfiguration {
        @ConditionalOnMissingBean(name = PasswordValidationService.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PasswordValidationService passwordValidationService(final CasConfigurationProperties casProperties,
                                                                   @Qualifier(PasswordHistoryService.BEAN_NAME)
                                                                   final PasswordHistoryService passwordHistoryService) {
            return new DefaultPasswordValidationService(casProperties, passwordHistoryService);
        }
    }

    @Configuration(value = "PasswordManagementHistoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementHistoryConfiguration {
        @ConditionalOnMissingBean(name = PasswordHistoryService.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
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
    static class PasswordManagementAuditConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementReturnValueResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver passwordManagementReturnValueResourceResolver() {
            return new ShortenedReturnValueAsStringAuditResourceResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordManagementAuditTrailRecordResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
    static class PasswordManagementCipherConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
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
    static class PasswordManagementCoreConfiguration {

        @ConditionalOnMissingBean(name = PasswordResetUrlBuilder.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PasswordResetUrlBuilder passwordResetUrlBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordChangeService")
            final PasswordManagementService passwordChangeService,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory) {
            return new DefaultPasswordResetUrlBuilder(passwordChangeService,
                ticketRegistry, ticketFactory, casProperties);
        }

        @ConditionalOnMissingBean(name = PasswordManagementService.DEFAULT_BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PasswordManagementService passwordChangeService(
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordManagementCipherExecutor")
            final CipherExecutor passwordManagementCipherExecutor,
            @Qualifier(PasswordHistoryService.BEAN_NAME)
            final PasswordHistoryService passwordHistoryService) {
            val pm = casProperties.getAuthn().getPm();
            if (pm.getCore().isEnabled()) {
                val location = pm.getJson().getLocation();
                if (location != null) {
                    LOGGER.debug("Configuring password management based on JSON resource [{}]", location);
                    return new JsonResourcePasswordManagementService(passwordManagementCipherExecutor,
                        casProperties, location, passwordHistoryService);
                }
                val groovyScript = pm.getGroovy().getLocation();
                if (groovyScript != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                    LOGGER.debug("Configuring password management based on Groovy resource [{}]", groovyScript);
                    return new GroovyResourcePasswordManagementService(passwordManagementCipherExecutor,
                        casProperties, groovyScript, passwordHistoryService);
                }
                LOGGER.warn("No storage service is configured to handle the account update and password service operations. "
                    + "Password management functionality will have no effect and will be disabled until a storage service is configured. "
                    + "To explicitly disable the password management, add 'cas.authn.pm.core.enabled=false' to the CAS configuration");
            } else {
                LOGGER.debug("Password management is disabled. To enable the password management functionality, "
                    + "add 'cas.authn.pm.core.enabled=true' to the CAS configuration and then configure storage options for account updates");
            }
            return new NoOpPasswordManagementService(passwordManagementCipherExecutor, casProperties);
        }

        @Bean
        @Lazy(false)
        public CasApplicationReadyListener passwordManagementApplicationReady(
            final CasConfigurationProperties casProperties,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final ObjectProvider<CommunicationsManager> communicationsManager) {
            return event -> {
                val pm = casProperties.getAuthn().getPm();
                if (pm.getCore().isEnabled()) {
                    communicationsManager.getObject().validate();
                }
            };
        }
    }

    @Configuration(value = "PasswordManagementSyncConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement, module = "password-sync")
    static class PasswordManagementSyncConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.password-sync.enabled")
            .isTrue().evenIfMissing().and("cas.authn.password-sync.rest.url").isUrl();

        @ConditionalOnMissingBean(name = "restfulPasswordSynchronizationAuthenticationPostProcessor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationPostProcessor restfulPasswordSynchronizationAuthenticationPostProcessor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuthenticationPostProcessor.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val properties = casProperties.getAuthn().getPasswordSync().getRest();
                    return new RestfulPasswordSynchronizationAuthenticationPostProcessor(properties);
                })
                .otherwise(AuthenticationPostProcessor::none)
                .get();
        }

        @ConditionalOnMissingBean(name = "restfulPasswordSynchronizationAuthenticationPostProcessorExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer restfulPasswordSynchronizationAuthenticationPostProcessorExecutionPlanConfigurer(
            @Qualifier("restfulPasswordSynchronizationAuthenticationPostProcessor")
            final AuthenticationPostProcessor restfulPasswordSynchronizationAuthenticationPostProcessor,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerAuthenticationPostProcessor(restfulPasswordSynchronizationAuthenticationPostProcessor))
                .otherwiseProxy()
                .get();
        }
    }
}
