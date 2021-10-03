package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.acct.AccountRegistrationPropertyLoader;
import org.apereo.cas.acct.AccountRegistrationRequestAuditPrincipalIdResolver;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationTokenCipherExecutor;
import org.apereo.cas.acct.AccountRegistrationUsernameBuilder;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.acct.DefaultAccountRegistrationPropertyLoader;
import org.apereo.cas.acct.DefaultAccountRegistrationService;
import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import org.apereo.cas.acct.provision.ChainingAccountRegistrationProvisioner;
import org.apereo.cas.acct.provision.GroovyAccountRegistrationProvisioner;
import org.apereo.cas.acct.provision.RestfulAccountRegistrationProvisioner;
import org.apereo.cas.acct.webflow.AccountManagementRegistrationCaptchaWebflowConfigurer;
import org.apereo.cas.acct.webflow.AccountManagementWebflowConfigurer;
import org.apereo.cas.acct.webflow.FinalizeAccountRegistrationAction;
import org.apereo.cas.acct.webflow.LoadAccountRegistrationPropertiesAction;
import org.apereo.cas.acct.webflow.SubmitAccountRegistrationAction;
import org.apereo.cas.acct.webflow.ValidateAccountRegistrationTokenAction;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasAccountManagementWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "CasAccountManagementWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAccountManagementWebflowConfiguration {

    @ConditionalOnMissingBean(name = "accountMgmtCipherExecutor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public CipherExecutor accountMgmtCipherExecutor(final CasConfigurationProperties casProperties) {
        val crypto = casProperties.getAccountRegistration().getCore().getCrypto();
        return crypto.isEnabled() ? CipherExecutorUtils.newStringCipherExecutor(crypto, AccountRegistrationTokenCipherExecutor.class) : CipherExecutor.noOp();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountRegistrationUsernameBuilder")
    public AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder() {
        return AccountRegistrationUsernameBuilder.asDefault();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountMgmtRegistrationService")
    @Autowired
    public AccountRegistrationService accountMgmtRegistrationService(
        final CasConfigurationProperties casProperties,
        @Qualifier("accountMgmtRegistrationPropertyLoader")
        final AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader,
        @Qualifier("accountMgmtCipherExecutor")
        final CipherExecutor accountMgmtCipherExecutor,
        @Qualifier("accountRegistrationUsernameBuilder")
        final AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder,
        @Qualifier("accountMgmtRegistrationProvisioner")
        final AccountRegistrationProvisioner accountMgmtRegistrationProvisioner) {
        return new DefaultAccountRegistrationService(accountMgmtRegistrationPropertyLoader, casProperties, accountMgmtCipherExecutor, accountRegistrationUsernameBuilder,
            accountMgmtRegistrationProvisioner);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountMgmtRegistrationProvisioner")
    @Autowired
    public AccountRegistrationProvisioner accountMgmtRegistrationProvisioner(
        final List<AccountRegistrationProvisionerConfigurer> beans) {
        val configurers = beans.stream().map(AccountRegistrationProvisionerConfigurer::configure).sorted().collect(Collectors.toList());
        return new ChainingAccountRegistrationProvisioner(configurers);
    }

    @Bean
    @ConditionalOnMissingBean(name = "accountMgmtRegistrationPropertyLoader")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader(final CasConfigurationProperties casProperties) {
        val resource = casProperties.getAccountRegistration().getCore().getRegistrationProperties().getLocation();
        return new DefaultAccountRegistrationPropertyLoader(resource);
    }

    @ConditionalOnMissingBean(name = "restfulAccountRegistrationProvisionerConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnProperty(name = "cas.account-registration.provisioning.rest.url")
    @Autowired
    public AccountRegistrationProvisionerConfigurer restfulAccountRegistrationProvisionerConfigurer(
        final CasConfigurationProperties casProperties) {
        return () -> {
            val props = casProperties.getAccountRegistration().getProvisioning().getRest();
            return new RestfulAccountRegistrationProvisioner(props);
        };
    }

    @ConditionalOnMissingBean(name = "groovyAccountRegistrationProvisionerConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnProperty(name = "cas.account-registration.provisioning.groovy.location")
    @Autowired
    public AccountRegistrationProvisionerConfigurer groovyAccountRegistrationProvisionerConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return () -> {
            val groovy = casProperties.getAccountRegistration().getProvisioning().getGroovy();
            return new GroovyAccountRegistrationProvisioner(new WatchableGroovyScriptResource(groovy.getLocation()), applicationContext);
        };
    }

    @Configuration(value = "CasAccountManagementWebflowCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasAccountManagementWebflowCoreConfiguration {
        @ConditionalOnMissingBean(name = "accountMgmtWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer accountMgmtWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new AccountManagementWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

    }

    @Configuration(value = "CasAccountManagementWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasAccountManagementWebflowPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "accountMgmtCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer accountMgmtCasWebflowExecutionPlanConfigurer(
            @Qualifier("accountMgmtWebflowConfigurer")
            final CasWebflowConfigurer accountMgmtWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(accountMgmtWebflowConfigurer);
        }
    }

    @Configuration(value = "CasAccountManagementWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasAccountManagementWebflowActionConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN)
        public Action validateAccountRegistrationTokenAction(
            @Qualifier("accountMgmtRegistrationService")
            final AccountRegistrationService accountMgmtRegistrationService,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new ValidateAccountRegistrationTokenAction(centralAuthenticationService, accountMgmtRegistrationService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST)
        @Bean
        public Action finalizeAccountRegistrationRequestAction(
            @Qualifier("accountMgmtRegistrationService")
            final AccountRegistrationService accountMgmtRegistrationService) {
            return new FinalizeAccountRegistrationAction(accountMgmtRegistrationService);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "loadAccountRegistrationPropertiesAction")
        public Action loadAccountRegistrationPropertiesAction(
            @Qualifier("accountMgmtRegistrationService")
            final AccountRegistrationService accountMgmtRegistrationService) {
            return new LoadAccountRegistrationPropertiesAction(accountMgmtRegistrationService);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "submitAccountRegistrationAction")
        @Autowired
        public Action submitAccountRegistrationAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("accountMgmtRegistrationService")
            final AccountRegistrationService accountMgmtRegistrationService,
            @Qualifier("defaultTicketFactory")
            final TicketFactory defaultTicketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager) {
            return new SubmitAccountRegistrationAction(accountMgmtRegistrationService, casProperties,
                communicationsManager, defaultTicketFactory, ticketRegistry);
        }
    }

    @Configuration(value = "CasAccountManagementWebflowAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasAccountManagementWebflowAuditConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "accountMgmtRegistrationAuditPrincipalIdResolver")
        public AuditPrincipalIdProvider accountMgmtRegistrationAuditPrincipalIdResolver(
            @Qualifier("accountMgmtRegistrationService")
            final AccountRegistrationService accountMgmtRegistrationService) {
            return new AccountRegistrationRequestAuditPrincipalIdResolver(accountMgmtRegistrationService);
        }


        @ConditionalOnMissingBean(name = "accountRegistrationAuditTrailRecordResolutionPlanConfigurer")
        @Bean
        public AuditTrailRecordResolutionPlanConfigurer accountRegistrationAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.ACCOUNT_REGISTRATION_TOKEN_VALIDATION_ACTION_RESOLVER,
                    new DefaultAuditActionResolver("_TOKEN" + AuditTrailConstants.AUDIT_ACTION_POSTFIX_VALIDATED, StringUtils.EMPTY));
                plan.registerAuditResourceResolver(AuditResourceResolvers.ACCOUNT_REGISTRATION_TOKEN_VALIDATION_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.ACCOUNT_REGISTRATION_TOKEN_CREATION_ACTION_RESOLVER,
                    new DefaultAuditActionResolver("_TOKEN" + AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, StringUtils.EMPTY));
                plan.registerAuditResourceResolver(AuditResourceResolvers.ACCOUNT_REGISTRATION_TOKEN_CREATION_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.ACCOUNT_REGISTRATION_PROVISIONING_ACTION_RESOLVER,
                    new DefaultAuditActionResolver("_PROVISIONING" + AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, StringUtils.EMPTY));
                plan.registerAuditResourceResolver(AuditResourceResolvers.ACCOUNT_REGISTRATION_PROVISIONING_RESOURCE_RESOLVER, returnValueResourceResolver);
            };
        }

    }

    @ConditionalOnProperty(prefix = "cas.account-registration.google-recaptcha", name = "enabled", havingValue = "true")
    @Configuration(value = "casAccountManagementRegistrationCaptchaConfiguration", proxyBeanMethods = false)
    public static class CasAccountManagementRegistrationCaptchaConfiguration {

        @ConditionalOnMissingBean(name = "accountMgmtRegistrationCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public CasWebflowConfigurer accountMgmtRegistrationCaptchaWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val configurer = new AccountManagementRegistrationCaptchaWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
            configurer.setOrder(casProperties.getAccountRegistration().getWebflow().getOrder() + 2);
            return configurer;
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_VALIDATE_CAPTCHA)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public Action accountMgmtRegistrationValidateCaptchaAction(final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAccountRegistration().getGoogleRecaptcha();
            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha));
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_INIT_CAPTCHA)
        public Action accountMgmtRegistrationInitializeCaptchaAction(final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAccountRegistration().getGoogleRecaptcha();
            return new InitializeCaptchaAction(recaptcha) {

                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    AccountRegistrationUtils.putAccountRegistrationCaptchaEnabled(requestContext, recaptcha);
                    return super.doExecute(requestContext);
                }
            };
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "accountMgmtRegistrationCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer accountMgmtRegistrationCaptchaWebflowExecutionPlanConfigurer(
            @Qualifier("accountMgmtRegistrationCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return plan -> plan.registerWebflowConfigurer(cfg);
        }
    }
}
