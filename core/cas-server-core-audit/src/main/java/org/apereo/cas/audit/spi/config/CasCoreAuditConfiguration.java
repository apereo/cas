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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasCoreAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreAuditConfiguration")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuditConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan;

    @Autowired
    @Qualifier("auditTrailRecordResolutionPlan")
    private ObjectProvider<AuditTrailRecordResolutionPlan> auditTrailRecordResolutionPlan;

    @Bean
    @ConditionalOnProperty(prefix = "cas.audit.engine", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "inMemoryAuditEventRepository")
    public AuditEventRepository inMemoryAuditEventRepository() {
        return new InMemoryAuditEventRepository();
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditTrailManagementAspect")
    @ConditionalOnProperty(prefix = "cas.audit.engine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditTrailManagementAspect auditTrailManagementAspect() {
        val audit = casProperties.getAudit().getEngine();
        val auditFormat = AbstractStringAuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
        val auditRecordResolutionPlan = auditTrailRecordResolutionPlan.getObject();
        val aspect = new AuditTrailManagementAspect(
            audit.getAppCode(),
            auditablePrincipalResolver(auditPrincipalIdProvider()),
            CollectionUtils.wrapList(filterAndDelegateAuditTrailManager()),
            auditRecordResolutionPlan.getAuditActionResolvers(),
            auditRecordResolutionPlan.getAuditResourceResolvers(),
            auditRecordResolutionPlan.getAuditPrincipalResolvers(),
            auditFormat);
        aspect.setFailOnAuditFailures(!audit.isIgnoreAuditFailures());
        return aspect;
    }

    @Autowired
    @ConditionalOnMissingBean(name = "auditTrailRecordResolutionPlan")
    @Bean
    public AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan(final List<AuditTrailRecordResolutionPlanConfigurer> configurers) {
        val plan = new DefaultAuditTrailRecordResolutionPlan();
        configurers.forEach(c -> {
            LOGGER.trace("Registering audit trail manager [{}]", c.getName());
            c.configureAuditTrailRecordResolutionPlan(plan);
        });
        return plan;
    }

    @Autowired
    @ConditionalOnMissingBean(name = "auditTrailExecutionPlan")
    @Bean
    public AuditTrailExecutionPlan auditTrailExecutionPlan(final List<AuditTrailExecutionPlanConfigurer> configurers) {
        val plan = new DefaultAuditTrailExecutionPlan();
        configurers.forEach(c -> {
            LOGGER.trace("Configuring audit trail execution plan via [{}]", c.getName());
            c.configureAuditTrailExecutionPlan(plan);
        });
        return plan;
    }

    @Bean
    public FilterRegistrationBean casClientInfoLoggingFilter() {
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

    @ConditionalOnMissingBean(name = "returnValueResourceResolver")
    @Bean
    public AuditResourceResolver returnValueResourceResolver() {
        return new ShortenedReturnValueAsStringAuditResourceResolver();
    }

    @ConditionalOnMissingBean(name = "nullableReturnValueResourceResolver")
    @Bean
    public AuditResourceResolver nullableReturnValueResourceResolver() {
        val resolver = new NullableReturnValueAuditResourceResolver(returnValueResourceResolver());
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

    /**
     * Extension point for deployers to define custom AuditActionResolvers to extend the stock resolvers.
     *
     * @return the map
     */
    @ConditionalOnMissingBean(name = "customAuditActionResolverMap")
    @Bean
    public Map<String, AuditActionResolver> customAuditActionResolverMap() {
        return new HashMap<>(0);
    }

    /**
     * Extension point for deployers to define custom AuditResourceResolvers to extend the stock resolvers.
     *
     * @return the map
     */
    @ConditionalOnMissingBean(name = "customAuditResourceResolverMap")
    @Bean
    public Map<String, AuditResourceResolver> customAuditResourceResolverMap() {
        return new HashMap<>(0);
    }

    @ConditionalOnMissingBean(name = "auditablePrincipalResolver")
    @Bean
    public PrincipalResolver auditablePrincipalResolver(@Qualifier("auditPrincipalIdProvider") final AuditPrincipalIdProvider auditPrincipalIdProvider) {
        return new ThreadLocalAuditPrincipalResolver(auditPrincipalIdProvider);
    }

    @ConditionalOnMissingBean(name = "ticketResourceResolver")
    @Bean
    public AuditResourceResolver ticketResourceResolver() {
        return new TicketAsFirstParameterResourceResolver();
    }

    @ConditionalOnMissingBean(name = "ticketValidationResourceResolver")
    @Bean
    public AuditResourceResolver ticketValidationResourceResolver() {
        if (casProperties.getAudit().getEngine().isIncludeValidationAssertion()) {
            return new TicketValidationResourceResolver();
        }
        return ticketResourceResolver();
    }

    @ConditionalOnMissingBean(name = "messageBundleAwareResourceResolver")
    @Bean
    public AuditResourceResolver messageBundleAwareResourceResolver() {
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
    @ConditionalOnMissingBean(name = "credentialsAsFirstParameterResourceResolver")
    public AuditResourceResolver credentialsAsFirstParameterResourceResolver() {
        return new CredentialsAsFirstParameterResourceResolver();
    }

    @ConditionalOnMissingBean(name = "auditPrincipalIdProvider")
    @Bean
    public AuditPrincipalIdProvider auditPrincipalIdProvider() {
        val resolvers = applicationContext.getBeansOfType(AuditPrincipalIdProvider.class, false, true);
        val providers = new ArrayList<>(resolvers.values());
        AnnotationAwareOrderComparator.sortIfNecessary(providers);
        return new ChainingAuditPrincipalIdProvider(providers);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAuditTrailExecutionPlanConfigurer")
    @ConditionalOnProperty(prefix = "cas.audit.slf4j", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditTrailExecutionPlanConfigurer casAuditTrailExecutionPlanConfigurer() {
        return plan -> {
            val slf4j = casProperties.getAudit().getSlf4j();
            val slf4jManager = new Slf4jLoggingAuditTrailManager();
            slf4jManager.setUseSingleLine(slf4j.isUseSingleLine());
            slf4jManager.setEntrySeparator(slf4j.getSinglelineSeparator());
            plan.registerAuditTrailManager(slf4jManager);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer casAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            addAuditActionResolvers(plan);
            addAuditResourceResolvers(plan);
            addAuditCustomResolvers(plan);
        };
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "protocolSpecificationValidationResourceResolver")
    public AuditResourceResolver protocolSpecificationValidationResourceResolver() {
        return new ProtocolSpecificationValidationAuditResourceResolver(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "booleanActionResolver")
    public AuditActionResolver booleanActionResolver() {
        return new BooleanAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
            AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED);
    }

    @Bean
    @ConditionalOnMissingBean(name = "filterAndDelegateAuditTrailManager")
    protected AuditTrailManager filterAndDelegateAuditTrailManager() {
        val audit = casProperties.getAudit().getEngine();
        val auditFormat = AbstractStringAuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().name());
        val auditManager = new FilterAndDelegateAuditTrailManager(
            auditTrailExecutionPlan.getObject().getAuditTrailManagers(),
            audit.getSupportedActions(), audit.getExcludedActions());
        auditManager.setAuditFormat(auditFormat);
        return auditManager;
    }

    private void addAuditCustomResolvers(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditActionResolvers(customAuditActionResolverMap());
        plan.registerAuditResourceResolvers(customAuditResourceResolverMap());
    }

    private void addAuditActionResolvers(final AuditTrailRecordResolutionPlan plan) {
        val resolver = authenticationActionResolver();
        plan.registerAuditActionResolver(AuditActionResolvers.AUTHENTICATION_RESOLVER, resolver);
        plan.registerAuditActionResolver(AuditActionResolvers.SAVE_SERVICE_ACTION_RESOLVER, resolver);
        plan.registerAuditActionResolver(AuditActionResolvers.DELETE_SERVICE_ACTION_RESOLVER, objectCreationAuditActionResolver());

        val defResolver = defaultAuditActionResolver();
        plan.registerAuditActionResolver(AuditActionResolvers.DESTROY_TICKET_RESOLVER, defResolver);
        plan.registerAuditActionResolver(AuditActionResolvers.DESTROY_PROXY_GRANTING_TICKET_RESOLVER, defResolver);

        val cResolver = ticketCreationActionResolver();
        plan.registerAuditActionResolver(AuditActionResolvers.CREATE_PROXY_GRANTING_TICKET_RESOLVER, cResolver);
        plan.registerAuditActionResolver(AuditActionResolvers.GRANT_SERVICE_TICKET_RESOLVER, cResolver);
        plan.registerAuditActionResolver(AuditActionResolvers.GRANT_PROXY_TICKET_RESOLVER, cResolver);
        plan.registerAuditActionResolver(AuditActionResolvers.CREATE_TICKET_GRANTING_TICKET_RESOLVER, cResolver);

        val triggeredResolver = triggeredAuditActionResolver();
        plan.registerAuditActionResolver(AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER, triggeredResolver);
        plan.registerAuditActionResolver(AuditActionResolvers.VALIDATE_SERVICE_TICKET_RESOLVER, ticketValidationActionResolver());
        plan.registerAuditActionResolver(AuditActionResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOLVER, booleanActionResolver());

        plan.registerAuditActionResolver(AuditActionResolvers.SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER, triggeredResolver);
    }

    private void addAuditResourceResolvers(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditResourceResolver(AuditResourceResolvers.AUTHENTICATION_RESOURCE_RESOLVER,
            credentialsAsFirstParameterResourceResolver());
        plan.registerAuditResourceResolver(AuditResourceResolvers.AUTHENTICATION_EVENT_RESOURCE_RESOLVER,
            nullableReturnValueResourceResolver());

        val messageBundleAwareResourceResolver = messageBundleAwareResourceResolver();
        plan.registerAuditResourceResolver(AuditResourceResolvers.CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER,
            messageBundleAwareResourceResolver);
        plan.registerAuditResourceResolver(AuditResourceResolvers.CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER,
            messageBundleAwareResourceResolver);

        val ticketResourceResolver = ticketResourceResolver();
        plan.registerAuditResourceResolver(AuditResourceResolvers.DESTROY_TICKET_RESOURCE_RESOLVER, ticketResourceResolver);
        plan.registerAuditResourceResolver(AuditResourceResolvers.DESTROY_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER, ticketResourceResolver);

        val serviceResolver = serviceAuditResourceResolver();
        plan.registerAuditResourceResolver(AuditResourceResolvers.GRANT_SERVICE_TICKET_RESOURCE_RESOLVER, serviceResolver);
        plan.registerAuditResourceResolver(AuditResourceResolvers.GRANT_PROXY_TICKET_RESOURCE_RESOLVER, serviceResolver);
        plan.registerAuditResourceResolver(AuditResourceResolvers.VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER, ticketValidationResourceResolver());
        plan.registerAuditResourceResolver(AuditResourceResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOURCE_RESOLVER,
            protocolSpecificationValidationResourceResolver());

        plan.registerAuditResourceResolver(AuditResourceResolvers.SAVE_SERVICE_RESOURCE_RESOLVER,
            returnValueResourceResolver());
        plan.registerAuditResourceResolver(AuditResourceResolvers.DELETE_SERVICE_RESOURCE_RESOLVER,
            returnValueResourceResolver());
        plan.registerAuditResourceResolver(AuditResourceResolvers.SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER,
            serviceAccessEnforcementAuditResourceResolver());
    }
}
