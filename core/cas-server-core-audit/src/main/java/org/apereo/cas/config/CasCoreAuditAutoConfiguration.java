package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.spi.plan.DefaultAuditTrailExecutionPlan;
import org.apereo.cas.audit.spi.plan.DefaultAuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.spi.principal.ChainingAuditPrincipalIdProvider;
import org.apereo.cas.audit.spi.principal.DefaultAuditPrincipalResolver;
import org.apereo.cas.audit.spi.resource.CredentialsAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.resource.LogoutRequestResourceResolver;
import org.apereo.cas.audit.spi.resource.ProtocolSpecificationValidationAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.ServiceAccessEnforcementAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.ServiceAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.TicketAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.resource.TicketValidationResourceResolver;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import org.apereo.cas.util.text.MessageSanitizer;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManagementAspect;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.FilterAndDelegateAuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.BooleanAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.MessageBundleAwareResourceResolver;
import org.apereo.inspektr.audit.spi.support.NullableReturnValueAuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.ObjectCreationAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.ShortenedReturnValueAsStringAuditResourceResolver;
import org.apereo.inspektr.audit.support.DelegatingAuditEventRepository;
import org.apereo.inspektr.audit.support.GroovyAuditTrailManager;
import org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.apereo.inspektr.common.spi.AuditActionDateProvider;
import org.apereo.inspektr.common.spi.ClientInfoResolver;
import org.apereo.inspektr.common.spi.DefaultClientInfoResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * This is {@link CasCoreAuditAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit)
@AutoConfiguration
public class CasCoreAuditAutoConfiguration {
    private static final BeanCondition CONDITION_AUDIT = BeanCondition.on("cas.audit.engine.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasCoreAuditPrincipalConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuditPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "auditablePrincipalResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolver auditablePrincipalResolver(
            @Qualifier(CasWebflowCredentialProvider.BEAN_NAME)
            final CasWebflowCredentialProvider casWebflowCredentialProvider,
            @Qualifier("auditPrincipalIdProvider")
            final AuditPrincipalIdProvider auditPrincipalIdProvider) {
            return new DefaultAuditPrincipalResolver(auditPrincipalIdProvider, casWebflowCredentialProvider);
        }

        @ConditionalOnMissingBean(name = "auditPrincipalIdProvider")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditPrincipalIdProvider auditPrincipalIdProvider(final List<AuditPrincipalIdProvider> providers) {
            AnnotationAwareOrderComparator.sortIfNecessary(providers);
            return new ChainingAuditPrincipalIdProvider(providers);
        }
    }

    @Configuration(value = "CasCoreAuditResourcesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuditResourcesConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "credentialsAsFirstParameterResourceResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver credentialsAsFirstParameterResourceResolver(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            final CasConfigurationProperties casProperties) {
            return new CredentialsAsFirstParameterResourceResolver(authenticationServiceSelectionPlan, casProperties.getAudit().getEngine());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "protocolSpecificationValidationResourceResolver")
        public AuditResourceResolver protocolSpecificationValidationResourceResolver(
            final CasConfigurationProperties casProperties) {
            return new ProtocolSpecificationValidationAuditResourceResolver(casProperties);
        }

        @ConditionalOnMissingBean(name = "ticketResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver ticketResourceResolver(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            final CasConfigurationProperties casProperties) {
            return new TicketAsFirstParameterResourceResolver(authenticationServiceSelectionPlan, casProperties.getAudit().getEngine());
        }

        @ConditionalOnMissingBean(name = "ticketValidationResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver ticketValidationResourceResolver(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("ticketResourceResolver")
            final AuditResourceResolver ticketResourceResolver,
            final CasConfigurationProperties casProperties) {
            if (casProperties.getAudit().getEngine().isIncludeValidationAssertion()) {
                return new TicketValidationResourceResolver(authenticationServiceSelectionPlan, casProperties.getAudit().getEngine());
            }
            return ticketResourceResolver;
        }

        @ConditionalOnMissingBean(name = "messageBundleAwareResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver messageBundleAwareResourceResolver(
            @Qualifier(MessageSanitizer.BEAN_NAME)
            final MessageSanitizer messageSanitizer,
            final ConfigurableApplicationContext applicationContext) {
            val resolver = new MessageBundleAwareResourceResolver(applicationContext);
            resolver.setResourcePostProcessor(inputs -> Arrays.stream(inputs)
                .map(messageSanitizer::sanitize)
                .toArray(String[]::new));
            return resolver;
        }

        @ConditionalOnMissingBean(name = "serviceAuditResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver serviceAuditResourceResolver(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            final CasConfigurationProperties casProperties) {
            return new ServiceAuditResourceResolver(authenticationServiceSelectionPlan, casProperties.getAudit().getEngine());
        }

        @ConditionalOnMissingBean(name = "returnValueResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver returnValueResourceResolver() {
            return new ShortenedReturnValueAsStringAuditResourceResolver();
        }

        @ConditionalOnMissingBean(name = "nullableReturnValueResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver nullableReturnValueResourceResolver(
            @Qualifier(MessageSanitizer.BEAN_NAME)
            final MessageSanitizer messageSanitizer) {
            val resolver = new NullableReturnValueAuditResourceResolver(new ShortenedReturnValueAsStringAuditResourceResolver());
            resolver.setResourcePostProcessor(inputs -> Arrays.stream(inputs)
                .map(messageSanitizer::sanitize)
                .toArray(String[]::new));
            return resolver;
        }

        @ConditionalOnMissingBean(name = "serviceAccessEnforcementAuditResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver serviceAccessEnforcementAuditResourceResolver(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            final CasConfigurationProperties casProperties) {
            return new ServiceAccessEnforcementAuditResourceResolver(authenticationServiceSelectionPlan, casProperties.getAudit().getEngine());
        }

        @ConditionalOnMissingBean(name = "logoutRequestResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver logoutRequestResourceResolver() {
            return new LogoutRequestResourceResolver();
        }
    }

    @Configuration(value = "CasCoreAuditActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuditActionsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "defaultAuditActionDateProvider")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionDateProvider defaultAuditActionDateProvider() {
            return AuditActionDateProvider.utc();
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultAuditActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver defaultAuditActionResolver() {
            return new DefaultAuditActionResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "triggeredAuditActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver triggeredAuditActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED);
        }

        @Bean
        @ConditionalOnMissingBean(name = "objectCreationAuditActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver objectCreationAuditActionResolver() {
            return new ObjectCreationAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @Bean
        @ConditionalOnMissingBean(name = "booleanActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver booleanActionResolver() {
            return new BooleanAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @ConditionalOnMissingBean(name = "authenticationActionResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver authenticationActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @ConditionalOnMissingBean(name = "ticketCreationActionResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver ticketCreationActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, "_NOT_CREATED");
        }

        @ConditionalOnMissingBean(name = "ticketValidationActionResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver ticketValidationActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @ConditionalOnMissingBean(name = "logoutAuditActionResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver logoutAuditActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }
    }

    @Configuration(value = "CasCoreAuditEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuditEventsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "auditEventRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditEventRepository auditEventRepository(
            @Qualifier(CasEventRepository.BEAN_NAME)
            final ObjectProvider<CasEventRepository> casEventRepository,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(AuditEventRepository.class)
                .when(CONDITION_AUDIT.given(applicationContext.getEnvironment()))
                .supply(() -> new DelegatingAuditEventRepository(casEventRepository))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreAuditManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreAuditManagementConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "auditTrailManagementAspect")
        public AuditTrailManagementAspect auditTrailManagementAspect(
            @Qualifier("defaultAuditActionDateProvider")
            final AuditActionDateProvider defaultAuditActionDateProvider,
            @Qualifier("casAuditClientInfoResolver")
            final ClientInfoResolver casAuditClientInfoResolver,
            @Qualifier("auditTrailRecordResolutionPlan")
            final AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan,
            @Qualifier("auditablePrincipalResolver")
            final PrincipalResolver auditablePrincipalResolver,
            @Qualifier("filterAndDelegateAuditTrailManager")
            final AuditTrailManager filterAndDelegateAuditTrailManager,
            final CasConfigurationProperties casProperties) {

            val audit = casProperties.getAudit().getEngine();
            val auditFormat = AuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
            val aspect = new AuditTrailManagementAspect(
                auditablePrincipalResolver,
                CollectionUtils.wrapList(filterAndDelegateAuditTrailManager),
                auditTrailRecordResolutionPlan.getAuditActionResolvers(),
                auditTrailRecordResolutionPlan.getAuditResourceResolvers(),
                auditTrailRecordResolutionPlan.getAuditPrincipalResolvers(),
                auditFormat,
                defaultAuditActionDateProvider);
            aspect.setFailOnAuditFailures(!audit.isIgnoreAuditFailures());
            aspect.setEnabled(casProperties.getAudit().getEngine().isEnabled());
            aspect.setClientInfoResolver(casAuditClientInfoResolver);
            return aspect;
        }

        @Bean
        @ConditionalOnMissingBean(name = "filterAndDelegateAuditTrailManager")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        protected AuditTrailManager filterAndDelegateAuditTrailManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
            final AuditTrailExecutionPlan auditTrailExecutionPlan,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuditTrailManager.class)
                .when(CONDITION_AUDIT.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val audit = casProperties.getAudit().getEngine();
                    val auditFormat = AuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
                    val auditManager = new FilterAndDelegateAuditTrailManager(
                        auditTrailExecutionPlan.getAuditTrailManagers(),
                        audit.getSupportedActions(), audit.getExcludedActions());
                    auditManager.setAuditFormat(auditFormat);
                    return auditManager;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "casAuditClientInfoResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ClientInfoResolver casAuditClientInfoResolver() {
            return new DefaultClientInfoResolver();
        }
    }

    @Configuration(value = "CasCoreAuditResolutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class CasCoreAuditResolutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "auditTrailRecordResolutionPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan(
            final List<AuditTrailRecordResolutionPlanConfigurer> configurers) {
            val plan = new DefaultAuditTrailRecordResolutionPlan();
            configurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .forEach(configurer -> {
                    LOGGER.trace("Registering audit trail manager [{}]", configurer.getName());
                    configurer.configureAuditTrailRecordResolutionPlan(plan);
                });
            return plan;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casAuditResourceResolversResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer casAuditResourceResolversResolutionPlanConfigurer(
            @Qualifier("credentialsAsFirstParameterResourceResolver")
            final AuditResourceResolver credentialsAsFirstParameterResourceResolver,
            @Qualifier("nullableReturnValueResourceResolver")
            final AuditResourceResolver nullableReturnValueResourceResolver,
            @Qualifier("messageBundleAwareResourceResolver")
            final AuditResourceResolver messageBundleAwareResourceResolver,
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver,
            @Qualifier("ticketResourceResolver")
            final AuditResourceResolver ticketResourceResolver,
            @Qualifier("serviceAccessEnforcementAuditResourceResolver")
            final AuditResourceResolver serviceAccessEnforcementAuditResourceResolver,
            @Qualifier("serviceAuditResourceResolver")
            final AuditResourceResolver serviceAuditResourceResolver,
            @Qualifier("ticketValidationResourceResolver")
            final AuditResourceResolver ticketValidationResourceResolver,
            @Qualifier("protocolSpecificationValidationResourceResolver")
            final AuditResourceResolver protocolSpecificationValidationResourceResolver,
            @Qualifier("logoutRequestResourceResolver")
            final AuditResourceResolver logoutRequestResourceResolver) {
            return plan -> {
                plan.registerAuditResourceResolver(AuditResourceResolvers.AUTHENTICATION_RESOURCE_RESOLVER, credentialsAsFirstParameterResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.AUTHENTICATION_EVENT_RESOURCE_RESOLVER, nullableReturnValueResourceResolver);

                plan.registerAuditResourceResolvers(messageBundleAwareResourceResolver,
                    AuditResourceResolvers.CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER,
                    AuditResourceResolvers.CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER);

                plan.registerAuditResourceResolvers(ticketResourceResolver,
                    AuditResourceResolvers.DESTROY_TICKET_RESOURCE_RESOLVER,
                    AuditResourceResolvers.DESTROY_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER);

                plan.registerAuditResourceResolver(serviceAuditResourceResolver,
                    AuditResourceResolvers.GRANT_SERVICE_TICKET_RESOURCE_RESOLVER,
                    AuditResourceResolvers.GRANT_PROXY_TICKET_RESOURCE_RESOLVER);

                plan.registerAuditResourceResolver(AuditResourceResolvers.VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER, ticketValidationResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOURCE_RESOLVER, protocolSpecificationValidationResourceResolver);

                plan.registerAuditResourceResolvers(returnValueResourceResolver,
                    AuditResourceResolvers.SAVE_SERVICE_RESOURCE_RESOLVER,
                    AuditResourceResolvers.DELETE_SERVICE_RESOURCE_RESOLVER);

                plan.registerAuditResourceResolver(AuditResourceResolvers.SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER, serviceAccessEnforcementAuditResourceResolver);

                plan.registerAuditResourceResolver(AuditResourceResolvers.LOGOUT_RESOURCE_RESOLVER, logoutRequestResourceResolver);
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "casAuditActionResolversResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer casAuditActionResolversResolutionPlanConfigurer(
            @Qualifier("authenticationActionResolver")
            final AuditActionResolver authenticationActionResolver,
            @Qualifier("objectCreationAuditActionResolver")
            final AuditActionResolver objectCreationAuditActionResolver,
            @Qualifier("defaultAuditActionResolver")
            final AuditActionResolver defaultAuditActionResolver,
            @Qualifier("ticketCreationActionResolver")
            final AuditActionResolver ticketCreationActionResolver,
            @Qualifier("triggeredAuditActionResolver")
            final AuditActionResolver triggeredAuditActionResolver,
            @Qualifier("ticketValidationActionResolver")
            final AuditActionResolver ticketValidationActionResolver,
            @Qualifier("booleanActionResolver")
            final AuditActionResolver booleanActionResolver,
            @Qualifier("logoutAuditActionResolver")
            final AuditActionResolver logoutAuditActionResolver) {
            return plan -> {
                plan.registerAuditActionResolvers(authenticationActionResolver,
                    AuditActionResolvers.AUTHENTICATION_RESOLVER,
                    AuditActionResolvers.SAVE_SERVICE_ACTION_RESOLVER);

                plan.registerAuditActionResolvers(ticketCreationActionResolver,
                    AuditActionResolvers.CREATE_PROXY_GRANTING_TICKET_RESOLVER,
                    AuditActionResolvers.GRANT_PROXY_TICKET_RESOLVER,
                    AuditActionResolvers.CREATE_TICKET_GRANTING_TICKET_RESOLVER,
                    AuditActionResolvers.GRANT_SERVICE_TICKET_RESOLVER);

                plan.registerAuditActionResolver(AuditActionResolvers.DELETE_SERVICE_ACTION_RESOLVER, objectCreationAuditActionResolver);

                plan.registerAuditActionResolvers(defaultAuditActionResolver,
                    AuditActionResolvers.DESTROY_TICKET_RESOLVER,
                    AuditActionResolvers.DESTROY_PROXY_GRANTING_TICKET_RESOLVER);

                plan.registerAuditActionResolvers(triggeredAuditActionResolver,
                    AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER,
                    AuditActionResolvers.SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER);

                plan.registerAuditActionResolver(AuditActionResolvers.VALIDATE_SERVICE_TICKET_RESOLVER, ticketValidationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOLVER, booleanActionResolver);

                plan.registerAuditActionResolver(AuditActionResolvers.LOGOUT_ACTION_RESOLVER, logoutAuditActionResolver);
            };
        }
    }

    @Configuration(value = "CasCoreAuditExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class CasCoreAuditExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = AuditTrailExecutionPlan.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailExecutionPlan auditTrailExecutionPlan(final List<AuditTrailExecutionPlanConfigurer> configurers) {
            val plan = new DefaultAuditTrailExecutionPlan();
            configurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .forEach(cfg -> {
                    LOGGER.trace("Configuring audit trail execution plan via [{}]", cfg.getName());
                    cfg.configureAuditTrailExecutionPlan(plan);
                });
            return plan;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casAuditTrailExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailExecutionPlanConfigurer casAuditTrailExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuditTrailExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.audit.slf4j.enabled").isTrue().evenIfMissing().given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val slf4j = casProperties.getAudit().getSlf4j();
                    val manager = new Slf4jLoggingAuditTrailManager();
                    manager.setUseSingleLine(slf4j.isUseSingleLine());
                    manager.setEntrySeparator(slf4j.getSinglelineSeparator());
                    if (!slf4j.getAuditableFields().isEmpty()) {
                        val fields = StringUtils.commaDelimitedListToSet(slf4j.getAuditableFields())
                            .stream()
                            .map(field -> AuditTrailManager.AuditableFields.valueOf(field.toUpperCase(Locale.ENGLISH)))
                            .toList();
                        manager.setAuditableFields(fields);
                    }
                    plan.registerAuditTrailManager(manager);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "casGroovyAuditTrailExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingGraalVMNativeImage
        public AuditTrailExecutionPlanConfigurer casGroovyAuditTrailExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuditTrailExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.audit.groovy.template.location")
                    .exists().given(applicationContext.getEnvironment()))
                .supply(() -> plan ->
                    FunctionUtils.doAndHandle(__ -> {
                        val templateFile = casProperties.getAudit().getGroovy().getTemplate().getLocation().getFile();
                        val mgr = new GroovyAuditTrailManager(templateFile, applicationContext);
                        plan.registerAuditTrailManager(mgr);
                    }))
                .otherwiseProxy()
                .get();
        }
    }

}
