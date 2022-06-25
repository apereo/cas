package org.apereo.cas.audit.spi.config;

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
import org.apereo.cas.audit.spi.principal.ThreadLocalAuditPrincipalResolver;
import org.apereo.cas.audit.spi.resource.CredentialsAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.resource.ProtocolSpecificationValidationAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.ServiceAccessEnforcementAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.ServiceAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.TicketAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.resource.TicketValidationResourceResolver;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.text.MessageSanitizer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import org.apereo.inspektr.audit.support.AbstractStringAuditTrailManager;
import org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link CasCoreAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit)
@AutoConfiguration(after = CasCoreServicesConfiguration.class)
public class CasCoreAuditConfiguration {
    private static final BeanCondition CONDITION_AUDIT = BeanCondition.on("cas.audit.engine.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasCoreAuditPrincipalConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "auditablePrincipalResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalResolver auditablePrincipalResolver(
            @Qualifier("auditPrincipalIdProvider")
            final AuditPrincipalIdProvider auditPrincipalIdProvider) {
            return new ThreadLocalAuditPrincipalResolver(auditPrincipalIdProvider);
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
    public static class CasCoreAuditResourcesConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "credentialsAsFirstParameterResourceResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver credentialsAsFirstParameterResourceResolver() {
            return new CredentialsAsFirstParameterResourceResolver();
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
        public AuditResourceResolver ticketResourceResolver() {
            return new TicketAsFirstParameterResourceResolver();
        }

        @ConditionalOnMissingBean(name = "ticketValidationResourceResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver ticketValidationResourceResolver(
            @Qualifier("ticketResourceResolver")
            final AuditResourceResolver ticketResourceResolver,
            final CasConfigurationProperties casProperties) {
            if (casProperties.getAudit().getEngine().isIncludeValidationAssertion()) {
                return new TicketValidationResourceResolver();
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
        public AuditResourceResolver serviceAuditResourceResolver() {
            return new ServiceAuditResourceResolver();
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
        public AuditResourceResolver serviceAccessEnforcementAuditResourceResolver() {
            return new ServiceAccessEnforcementAuditResourceResolver();
        }

    }

    @Configuration(value = "CasCoreAuditActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditActionsConfiguration {
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

    }

    @Configuration(value = "CasCoreAuditEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditEventsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "inMemoryAuditEventRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditEventRepository inMemoryAuditEventRepository(
            final ConfigurableApplicationContext applicationContext) throws Exception {
            return BeanSupplier.of(AuditEventRepository.class)
                .when(CONDITION_AUDIT.given(applicationContext.getEnvironment()))
                .supply(InMemoryAuditEventRepository::new)
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreAuditManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditManagementConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "auditTrailManagementAspect")
        public AuditTrailManagementAspect auditTrailManagementAspect(
            @Qualifier("auditTrailRecordResolutionPlan")
            final AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan,
            @Qualifier("auditablePrincipalResolver")
            final PrincipalResolver auditablePrincipalResolver,
            @Qualifier("filterAndDelegateAuditTrailManager")
            final AuditTrailManager filterAndDelegateAuditTrailManager,
            final CasConfigurationProperties casProperties) {

            val audit = casProperties.getAudit().getEngine();
            val auditFormat = AbstractStringAuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
            val aspect = new AuditTrailManagementAspect(
                audit.getAppCode(),
                auditablePrincipalResolver,
                CollectionUtils.wrapList(filterAndDelegateAuditTrailManager),
                auditTrailRecordResolutionPlan.getAuditActionResolvers(),
                auditTrailRecordResolutionPlan.getAuditResourceResolvers(),
                auditTrailRecordResolutionPlan.getAuditPrincipalResolvers(),
                auditFormat);
            aspect.setFailOnAuditFailures(!audit.isIgnoreAuditFailures());
            aspect.setEnabled(casProperties.getAudit().getEngine().isEnabled());
            return aspect;
        }

        @Bean
        @ConditionalOnMissingBean(name = "filterAndDelegateAuditTrailManager")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        protected AuditTrailManager filterAndDelegateAuditTrailManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
            final AuditTrailExecutionPlan auditTrailExecutionPlan,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(AuditTrailManager.class)
                .when(CONDITION_AUDIT.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val audit = casProperties.getAudit().getEngine();
                    val auditFormat = AbstractStringAuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
                    val auditManager = new FilterAndDelegateAuditTrailManager(
                        auditTrailExecutionPlan.getAuditTrailManagers(),
                        audit.getSupportedActions(), audit.getExcludedActions());
                    auditManager.setAuditFormat(auditFormat);
                    return auditManager;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasCoreAuditFiltersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditFiltersConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<ClientInfoThreadLocalFilter> casClientInfoLoggingFilter(
            final CasConfigurationProperties casProperties) {
            val audit = casProperties.getAudit().getEngine();

            val bean = new FilterRegistrationBean<ClientInfoThreadLocalFilter>();
            bean.setFilter(new ClientInfoThreadLocalFilter());
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("CAS Client Info Logging Filter");
            bean.setAsyncSupported(true);
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

            val initParams = new HashMap<String, String>();
            if (StringUtils.isNotBlank(audit.getAlternateClientAddrHeaderName())) {
                initParams.put(ClientInfoThreadLocalFilter.CONST_IP_ADDRESS_HEADER, audit.getAlternateClientAddrHeaderName());
            }

            if (StringUtils.isNotBlank(audit.getAlternateServerAddrHeaderName())) {
                initParams.put(ClientInfoThreadLocalFilter.CONST_SERVER_IP_ADDRESS_HEADER, audit.getAlternateServerAddrHeaderName());
            }

            initParams.put(ClientInfoThreadLocalFilter.CONST_USE_SERVER_HOST_ADDRESS, String.valueOf(audit.isUseServerHostAddress()));
            bean.setInitParameters(initParams);
            return bean;
        }
    }

    @Configuration(value = "CasCoreAuditResolutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class CasCoreAuditResolutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "auditTrailRecordResolutionPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan(
            final List<AuditTrailRecordResolutionPlanConfigurer> configurers) {
            val plan = new DefaultAuditTrailRecordResolutionPlan();
            configurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .forEach(c -> {
                    LOGGER.trace("Registering audit trail manager [{}]", c.getName());
                    c.configureAuditTrailRecordResolutionPlan(plan);
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
            final AuditResourceResolver protocolSpecificationValidationResourceResolver) {
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
            final AuditActionResolver booleanActionResolver) {
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
            };
        }
    }

    @Configuration(value = "CasCoreAuditExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class CasCoreAuditExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = AuditTrailExecutionPlan.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailExecutionPlan auditTrailExecutionPlan(final List<AuditTrailExecutionPlanConfigurer> configurers) {
            val plan = new DefaultAuditTrailExecutionPlan();
            configurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .forEach(c -> {
                    LOGGER.trace("Configuring audit trail execution plan via [{}]", c.getName());
                    c.configureAuditTrailExecutionPlan(plan);
                });
            return plan;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casAuditTrailExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailExecutionPlanConfigurer casAuditTrailExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(AuditTrailExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.audit.slf4j.enabled").isTrue().evenIfMissing().given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val slf4j = casProperties.getAudit().getSlf4j();
                    val slf4jManager = new Slf4jLoggingAuditTrailManager();
                    slf4jManager.setUseSingleLine(slf4j.isUseSingleLine());
                    slf4jManager.setEntrySeparator(slf4j.getSinglelineSeparator());
                    plan.registerAuditTrailManager(slf4jManager);
                })
                .otherwiseProxy()
                .get();
        }
    }

}
