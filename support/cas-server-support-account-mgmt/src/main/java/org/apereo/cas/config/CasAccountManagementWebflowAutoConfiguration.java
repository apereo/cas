package org.apereo.cas.config;

import org.apereo.cas.acct.AccountRegistrationPropertyLoader;
import org.apereo.cas.acct.AccountRegistrationRequestAuditPrincipalIdResolver;
import org.apereo.cas.acct.AccountRegistrationRequestValidator;
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
import org.apereo.cas.acct.provision.ScimAccountRegistrationProvisioner;
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
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.scim.v2.provisioning.ScimPrincipalAttributeMapper;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.DefaultCaptchaActivationStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasAccountManagementWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountRegistration)
@AutoConfiguration
public class CasAccountManagementWebflowAutoConfiguration {

    @ConditionalOnMissingBean(name = "accountMgmtCipherExecutor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
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
    @ConditionalOnMissingBean(name = AccountRegistrationService.BEAN_NAME)
    public AccountRegistrationService accountMgmtRegistrationService(
        @Qualifier("accountMgmtRegistrationRequestValidator")
        final AccountRegistrationRequestValidator accountMgmtRegistrationRequestValidator,
        final CasConfigurationProperties casProperties,
        @Qualifier("accountMgmtRegistrationPropertyLoader")
        final AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader,
        @Qualifier("accountMgmtCipherExecutor")
        final CipherExecutor accountMgmtCipherExecutor,
        @Qualifier("accountRegistrationUsernameBuilder")
        final AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder,
        @Qualifier(AccountRegistrationProvisioner.BEAN_NAME)
        final AccountRegistrationProvisioner accountMgmtRegistrationProvisioner) {
        return new DefaultAccountRegistrationService(
            accountMgmtRegistrationPropertyLoader, casProperties,
            accountMgmtCipherExecutor, accountRegistrationUsernameBuilder,
            accountMgmtRegistrationProvisioner, accountMgmtRegistrationRequestValidator);
    }

    @Bean
    @ConditionalOnMissingBean(name = "accountMgmtRegistrationPropertyLoader")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader(final CasConfigurationProperties casProperties) {
        val resource = casProperties.getAccountRegistration().getCore().getRegistrationProperties().getLocation();
        return new DefaultAccountRegistrationPropertyLoader(resource);
    }

    @Bean
    @ConditionalOnMissingBean(name = "accountMgmtRegistrationRequestValidator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AccountRegistrationRequestValidator accountMgmtRegistrationRequestValidator() {
        return AccountRegistrationRequestValidator.noOp();
    }

    @Configuration(value = "CasAccountManagementProvisioningConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAccountManagementProvisioningConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = AccountRegistrationProvisioner.BEAN_NAME)
        public AccountRegistrationProvisioner accountMgmtRegistrationProvisioner(
            final List<AccountRegistrationProvisionerConfigurer> beans) {
            val configurers = beans.stream()
                .filter(BeanSupplier::isNotProxy)
                .map(AccountRegistrationProvisionerConfigurer::configure)
                .sorted()
                .collect(Collectors.toList());
            return new ChainingAccountRegistrationProvisioner(configurers);
        }

        @ConditionalOnMissingBean(name = "restfulAccountRegistrationProvisionerConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccountRegistrationProvisionerConfigurer restfulAccountRegistrationProvisionerConfigurer(
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AccountRegistrationProvisionerConfigurer.class)
                .when(BeanCondition.on("cas.account-registration.provisioning.rest.url").isUrl().given(applicationContext.getEnvironment()))
                .supply(() -> () -> {
                    val props = casProperties.getAccountRegistration().getProvisioning().getRest();
                    return new RestfulAccountRegistrationProvisioner(httpClient, props);
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "groovyAccountRegistrationProvisionerConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingGraalVMNativeImage
        public AccountRegistrationProvisionerConfigurer groovyAccountRegistrationProvisionerConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(AccountRegistrationProvisionerConfigurer.class)
                .when(BeanCondition.on("cas.account-registration.provisioning.groovy.location")
                    .exists().given(applicationContext.getEnvironment()))
                .supply(() -> () -> {
                    val groovy = casProperties.getAccountRegistration().getProvisioning().getGroovy();
                    val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                    return new GroovyAccountRegistrationProvisioner(
                        scriptFactory.fromResource(groovy.getLocation()), applicationContext);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @ConditionalOnClass(ScimPrincipalAttributeMapper.class)
    @Configuration(value = "CasAccountManagementScimProvisioningConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountRegistration, module = "scim")
    static class CasAccountManagementScimProvisioningConfiguration {
        @ConditionalOnMissingBean(name = "scimAccountRegistrationProvisionerConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AccountRegistrationProvisionerConfigurer scimAccountRegistrationProvisionerConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalProvisioner.BEAN_NAME)
            final ObjectProvider<PrincipalProvisioner> principalProvisioners) {
            return BeanSupplier.of(AccountRegistrationProvisionerConfigurer.class)
                .when(BeanCondition.on("cas.account-registration.provisioning.scim.enabled").isTrue()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> () -> new ScimAccountRegistrationProvisioner(principalProvisioners.getObject(),
                    PrincipalFactoryUtils.newPrincipalFactory()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasAccountManagementWebflowCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAccountManagementWebflowCoreConfiguration {
        @ConditionalOnMissingBean(name = "accountMgmtWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer accountMgmtWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new AccountManagementWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        }

    }

    @Configuration(value = "CasAccountManagementWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAccountManagementWebflowPlanConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "accountMgmtCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer accountMgmtCasWebflowExecutionPlanConfigurer(
            @Qualifier("accountMgmtWebflowConfigurer")
            final CasWebflowConfigurer accountMgmtWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(accountMgmtWebflowConfigurer);
        }
    }

    @Configuration(value = "CasAccountManagementWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAccountManagementWebflowActionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN)
        public Action validateAccountRegistrationTokenAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties,
            @Qualifier(AccountRegistrationService.BEAN_NAME)
            final AccountRegistrationService accountMgmtRegistrationService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new ValidateAccountRegistrationTokenAction(ticketRegistry, accountMgmtRegistrationService))
                .withId(CasWebflowConstants.ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST)
        @Bean
        public Action finalizeAccountRegistrationRequestAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AccountRegistrationService.BEAN_NAME)
            final AccountRegistrationService accountMgmtRegistrationService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new FinalizeAccountRegistrationAction(accountMgmtRegistrationService))
                .withId(CasWebflowConstants.ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOAD_ACCOUNT_REGISTRATION_PROPERTIES)
        public Action loadAccountRegistrationPropertiesAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AccountRegistrationService.BEAN_NAME)
            final AccountRegistrationService accountMgmtRegistrationService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new LoadAccountRegistrationPropertiesAction(accountMgmtRegistrationService))
                .withId(CasWebflowConstants.ACTION_ID_LOAD_ACCOUNT_REGISTRATION_PROPERTIES)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT)
        public Action submitAccountRegistrationAction(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AccountRegistrationService.BEAN_NAME)
            final AccountRegistrationService accountMgmtRegistrationService,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory defaultTicketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SubmitAccountRegistrationAction(accountMgmtRegistrationService, casProperties,
                    communicationsManager, defaultTicketFactory, ticketRegistry, tenantExtractor))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasAccountManagementWebflowAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAccountManagementWebflowAuditConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "accountMgmtRegistrationAuditPrincipalIdResolver")
        public AuditPrincipalIdProvider accountMgmtRegistrationAuditPrincipalIdResolver(
            @Qualifier(AccountRegistrationService.BEAN_NAME)
            final AccountRegistrationService accountMgmtRegistrationService) {
            return new AccountRegistrationRequestAuditPrincipalIdResolver(accountMgmtRegistrationService);
        }


        @ConditionalOnMissingBean(name = "accountRegistrationAuditTrailRecordResolutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer accountRegistrationAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.ACCOUNT_REGISTRATION_TOKEN_VALIDATION_ACTION_RESOLVER,
                    new DefaultAuditActionResolver("_TOKEN" + AuditTrailConstants.AUDIT_ACTION_POSTFIX_VALIDATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.ACCOUNT_REGISTRATION_TOKEN_VALIDATION_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.ACCOUNT_REGISTRATION_TOKEN_CREATION_ACTION_RESOLVER,
                    new DefaultAuditActionResolver("_TOKEN" + AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
                plan.registerAuditResourceResolver(AuditResourceResolvers.ACCOUNT_REGISTRATION_TOKEN_CREATION_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.ACCOUNT_REGISTRATION_PROVISIONING_ACTION_RESOLVER,
                    new DefaultAuditActionResolver("_PROVISIONING" + AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS));
                plan.registerAuditResourceResolver(AuditResourceResolvers.ACCOUNT_REGISTRATION_PROVISIONING_RESOURCE_RESOLVER, returnValueResourceResolver);
            };
        }

    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountRegistration, module = "captcha")
    @Configuration(value = "CasAccountManagementRegistrationCaptchaConfiguration", proxyBeanMethods = false)
    static class CasAccountManagementRegistrationCaptchaConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.account-registration.google-recaptcha.enabled").isTrue();

        @ConditionalOnMissingBean(name = "accountMgmtRegistrationCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowConfigurer accountMgmtRegistrationCaptchaWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val configurer = new AccountManagementRegistrationCaptchaWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, applicationContext, casProperties);
                    configurer.setOrder(casProperties.getAccountRegistration().getWebflow().getOrder() + 2);
                    return configurer;
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_VALIDATE_CAPTCHA)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action accountMgmtRegistrationValidateCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("accountMgmtRegistrationCaptchaActivationStrategy")
            final CaptchaActivationStrategy accountMgmtRegistrationCaptchaActivationStrategy) {

            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() ->
                    WebflowActionBeanSupplier.builder()
                        .withApplicationContext(applicationContext)
                        .withProperties(casProperties)
                        .withAction(() -> {
                            val recaptcha = casProperties.getAccountRegistration().getGoogleRecaptcha();
                            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha),
                                accountMgmtRegistrationCaptchaActivationStrategy);
                        })
                        .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_VALIDATE_CAPTCHA)
                        .build()
                        .get())
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "accountMgmtRegistrationCaptchaActivationStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CaptchaActivationStrategy accountMgmtRegistrationCaptchaActivationStrategy(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return BeanSupplier.of(CaptchaActivationStrategy.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultCaptchaActivationStrategy(servicesManager))
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_INIT_CAPTCHA)
        public Action accountMgmtRegistrationInitializeCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("accountMgmtRegistrationCaptchaActivationStrategy")
            final CaptchaActivationStrategy accountMgmtRegistrationCaptchaActivationStrategy,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val recaptcha = casProperties.getAccountRegistration().getGoogleRecaptcha();
                    return new InitializeCaptchaAction(accountMgmtRegistrationCaptchaActivationStrategy,
                        requestContext -> AccountRegistrationUtils.putAccountRegistrationCaptchaEnabled(requestContext, recaptcha),
                        recaptcha);
                })
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "accountMgmtRegistrationCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer accountMgmtRegistrationCaptchaWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("accountMgmtRegistrationCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(cfg))
                .otherwiseProxy()
                .get();
        }
    }
}
