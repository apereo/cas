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
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.MessageSanitizationUtils;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Configuration(value = "casCoreAuditConfiguration", proxyBeanMethods = false)
@EnableAspectJAutoProxy
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@AutoConfigureAfter(CasCoreServicesConfiguration.class)
public class CasCoreAuditConfiguration {

    @Configuration(value = "CasCoreAuditPrincipalConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "auditablePrincipalResolver")
        @Bean
        public PrincipalResolver auditablePrincipalResolver(
            @Qualifier("auditPrincipalIdProvider")
            final AuditPrincipalIdProvider auditPrincipalIdProvider) {
            return new ThreadLocalAuditPrincipalResolver(auditPrincipalIdProvider);
        }

        @ConditionalOnMissingBean(name = "auditPrincipalIdProvider")
        @Bean
        @Autowired
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
        public AuditResourceResolver credentialsAsFirstParameterResourceResolver() {
            return new CredentialsAsFirstParameterResourceResolver();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "protocolSpecificationValidationResourceResolver")
        @Autowired
        public AuditResourceResolver protocolSpecificationValidationResourceResolver(
            final CasConfigurationProperties casProperties) {
            return new ProtocolSpecificationValidationAuditResourceResolver(casProperties);
        }

        @ConditionalOnMissingBean(name = "ticketResourceResolver")
        @Bean
        public AuditResourceResolver ticketResourceResolver() {
            return new TicketAsFirstParameterResourceResolver();
        }

        @ConditionalOnMissingBean(name = "ticketValidationResourceResolver")
        @Bean
        @Autowired
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
        @Autowired
        public AuditResourceResolver messageBundleAwareResourceResolver(
            final ConfigurableApplicationContext applicationContext) {
            val resolver = new MessageBundleAwareResourceResolver(applicationContext);
            resolver.setResourcePostProcessor(inputs -> Arrays.stream(inputs)
                .map(MessageSanitizationUtils::sanitize)
                .toArray(String[]::new));
            return resolver;
        }

        @ConditionalOnMissingBean(name = "serviceAuditResourceResolver")
        @Bean
        public AuditResourceResolver serviceAuditResourceResolver() {
            return new ServiceAuditResourceResolver();
        }


        @ConditionalOnMissingBean(name = "returnValueResourceResolver")
        @Bean
        public AuditResourceResolver returnValueResourceResolver() {
            return new ShortenedReturnValueAsStringAuditResourceResolver();
        }

        @ConditionalOnMissingBean(name = "nullableReturnValueResourceResolver")
        @Bean
        @Autowired
        public AuditResourceResolver nullableReturnValueResourceResolver() {
            val resolver = new NullableReturnValueAuditResourceResolver(new ShortenedReturnValueAsStringAuditResourceResolver());
            resolver.setResourcePostProcessor(inputs -> Arrays.stream(inputs)
                .map(MessageSanitizationUtils::sanitize)
                .toArray(String[]::new));
            return resolver;
        }

        @ConditionalOnMissingBean(name = "serviceAccessEnforcementAuditResourceResolver")
        @Bean
        public AuditResourceResolver serviceAccessEnforcementAuditResourceResolver() {
            return new ServiceAccessEnforcementAuditResourceResolver();
        }

    }

    @Configuration(value = "CasCoreAuditActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditActionsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "defaultAuditActionResolver")
        public AuditActionResolver defaultAuditActionResolver() {
            return new DefaultAuditActionResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "triggeredAuditActionResolver")
        public AuditActionResolver triggeredAuditActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY);
        }


        @Bean
        @ConditionalOnMissingBean(name = "objectCreationAuditActionResolver")
        public AuditActionResolver objectCreationAuditActionResolver() {
            return new ObjectCreationAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @Bean
        @ConditionalOnMissingBean(name = "booleanActionResolver")
        public AuditActionResolver booleanActionResolver() {
            return new BooleanAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @ConditionalOnMissingBean(name = "authenticationActionResolver")
        @Bean
        public AuditActionResolver authenticationActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

        @ConditionalOnMissingBean(name = "ticketCreationActionResolver")
        @Bean
        public AuditActionResolver ticketCreationActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, "_NOT_CREATED");
        }

        @ConditionalOnMissingBean(name = "ticketValidationActionResolver")
        @Bean
        public AuditActionResolver ticketValidationActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
        }

    }

    @Configuration(value = "CasCoreAuditEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditEventsConfiguration {
        @Bean
        @ConditionalOnProperty(prefix = "cas.audit.engine", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "inMemoryAuditEventRepository")
        public AuditEventRepository inMemoryAuditEventRepository() {
            return new InMemoryAuditEventRepository();
        }

    }

    @Configuration(value = "CasCoreAuditManagementConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditManagementConfiguration {

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "auditTrailManagementAspect")
        @ConditionalOnProperty(prefix = "cas.audit.engine", name = "enabled", havingValue = "true", matchIfMissing = true)
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
            return aspect;
        }

        @Bean
        @ConditionalOnMissingBean(name = "filterAndDelegateAuditTrailManager")
        @Autowired
        protected AuditTrailManager filterAndDelegateAuditTrailManager(
            @Qualifier("auditTrailExecutionPlan")
            final AuditTrailExecutionPlan auditTrailExecutionPlan,
            final CasConfigurationProperties casProperties) {
            val audit = casProperties.getAudit().getEngine();
            val auditFormat = AbstractStringAuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
            val auditManager = new FilterAndDelegateAuditTrailManager(
                auditTrailExecutionPlan.getAuditTrailManagers(),
                audit.getSupportedActions(), audit.getExcludedActions());
            auditManager.setAuditFormat(auditFormat);
            return auditManager;
        }
    }

    @Configuration(value = "CasCoreAuditFiltersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuditFiltersConfiguration {
        @Bean
        @Autowired
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
        @Autowired
        @ConditionalOnMissingBean(name = "auditTrailRecordResolutionPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan(
            final List<AuditTrailRecordResolutionPlanConfigurer> configurers) {
            val plan = new DefaultAuditTrailRecordResolutionPlan();
            configurers.forEach(c -> {
                LOGGER.trace("Registering audit trail manager [{}]", c.getName());
                c.configureAuditTrailRecordResolutionPlan(plan);
            });
            return plan;
        }
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "casAuditResourceResolversResolutionPlanConfigurer")
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
                plan.registerAuditResourceResolver(AuditResourceResolvers.CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER, messageBundleAwareResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER, messageBundleAwareResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.DESTROY_TICKET_RESOURCE_RESOLVER, ticketResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.DESTROY_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER, ticketResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.GRANT_SERVICE_TICKET_RESOURCE_RESOLVER, serviceAuditResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.GRANT_PROXY_TICKET_RESOURCE_RESOLVER, serviceAuditResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER, ticketValidationResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOURCE_RESOLVER, protocolSpecificationValidationResourceResolver);

                plan.registerAuditResourceResolver(AuditResourceResolvers.SAVE_SERVICE_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.DELETE_SERVICE_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER, serviceAccessEnforcementAuditResourceResolver);
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "casAuditActionResolversResolutionPlanConfigurer")
        @Autowired
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
                plan.registerAuditActionResolver(AuditActionResolvers.AUTHENTICATION_RESOLVER, authenticationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.SAVE_SERVICE_ACTION_RESOLVER, authenticationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.DELETE_SERVICE_ACTION_RESOLVER, objectCreationAuditActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.DESTROY_TICKET_RESOLVER, defaultAuditActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.DESTROY_PROXY_GRANTING_TICKET_RESOLVER, defaultAuditActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.CREATE_PROXY_GRANTING_TICKET_RESOLVER, ticketCreationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.GRANT_SERVICE_TICKET_RESOLVER, ticketCreationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.GRANT_PROXY_TICKET_RESOLVER, ticketCreationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.CREATE_TICKET_GRANTING_TICKET_RESOLVER, ticketCreationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER, triggeredAuditActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.VALIDATE_SERVICE_TICKET_RESOLVER, ticketValidationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOLVER, booleanActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER, triggeredAuditActionResolver);
            };
        }
    }
    
    @Configuration(value = "CasCoreAuditExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class CasCoreAuditExecutionPlanConfiguration {
        @Autowired
        @ConditionalOnMissingBean(name = "auditTrailExecutionPlan")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailExecutionPlan auditTrailExecutionPlan(final List<AuditTrailExecutionPlanConfigurer> configurers) {
            val plan = new DefaultAuditTrailExecutionPlan();
            configurers.forEach(c -> {
                LOGGER.trace("Configuring audit trail execution plan via [{}]", c.getName());
                c.configureAuditTrailExecutionPlan(plan);
            });
            return plan;
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "casAuditTrailExecutionPlanConfigurer")
        @ConditionalOnProperty(prefix = "cas.audit.slf4j", name = "enabled", havingValue = "true", matchIfMissing = true)
        public AuditTrailExecutionPlanConfigurer casAuditTrailExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties) {
            return plan -> {
                val slf4j = casProperties.getAudit().getSlf4j();
                val slf4jManager = new Slf4jLoggingAuditTrailManager();
                slf4jManager.setUseSingleLine(slf4j.isUseSingleLine());
                slf4jManager.setEntrySeparator(slf4j.getSinglelineSeparator());
                plan.registerAuditTrailManager(slf4jManager);
            };
        }
    }

}
