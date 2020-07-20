package org.apereo.cas.audit.spi.config;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.spi.FilterAndDelegateAuditTrailManager;
import org.apereo.cas.audit.spi.plan.DefaultAuditTrailExecutionPlan;
import org.apereo.cas.audit.spi.plan.DefaultAuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.spi.principal.ChainingAuditPrincipalIdProvider;
import org.apereo.cas.audit.spi.principal.ThreadLocalPrincipalResolver;
import org.apereo.cas.audit.spi.resource.CredentialsAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.resource.MessageBundleAwareResourceResolver;
import org.apereo.cas.audit.spi.resource.NullableReturnValueAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.ServiceAccessEnforcementAuditResourceResolver;
import org.apereo.cas.audit.spi.resource.ServiceResourceResolver;
import org.apereo.cas.audit.spi.resource.ShortenedReturnValueAsStringResourceResolver;
import org.apereo.cas.audit.spi.resource.TicketAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.resource.TicketValidationResourceResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.AuditTrailManagementAspect;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.ObjectCreationAuditActionResolver;
import org.apereo.inspektr.audit.support.AbstractStringAuditTrailManager;
import org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
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
@ConditionalOnProperty(prefix = "cas.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    public AuditTrailManagementAspect auditTrailManagementAspect() {
        val audit = casProperties.getAudit();
        val auditManager = new FilterAndDelegateAuditTrailManager(auditTrailExecutionPlan.getObject().getAuditTrailManagers(),
            audit.getSupportedActions(), audit.getExcludedActions());
        val auditRecordResolutionPlan = auditTrailRecordResolutionPlan.getObject();
        val aspect = new AuditTrailManagementAspect(
            audit.getAppCode(),
            auditablePrincipalResolver(auditPrincipalIdProvider()),
            CollectionUtils.wrapList(auditManager),
            auditRecordResolutionPlan.getAuditActionResolvers(),
            auditRecordResolutionPlan.getAuditResourceResolvers());
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
        val audit = casProperties.getAudit();

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
        return new ShortenedReturnValueAsStringResourceResolver();
    }

    @ConditionalOnMissingBean(name = "nullableReturnValueResourceResolver")
    @Bean
    public AuditResourceResolver nullableReturnValueResourceResolver() {
        return new NullableReturnValueAuditResourceResolver(returnValueResourceResolver());
    }

    @ConditionalOnMissingBean(name = "serviceAccessEnforcementAuditResourceResolver")
    @Bean
    public ServiceAccessEnforcementAuditResourceResolver serviceAccessEnforcementAuditResourceResolver() {
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
        return new ThreadLocalPrincipalResolver(auditPrincipalIdProvider);
    }

    @ConditionalOnMissingBean(name = "ticketResourceResolver")
    @Bean
    public AuditResourceResolver ticketResourceResolver() {
        return new TicketAsFirstParameterResourceResolver();
    }

    @ConditionalOnMissingBean(name = "ticketValidationResourceResolver")
    @Bean
    public AuditResourceResolver ticketValidationResourceResolver() {
        val audit = casProperties.getAudit();
        if (audit.isIncludeValidationAssertion()) {
            return new TicketValidationResourceResolver();
        }
        return ticketResourceResolver();
    }

    @ConditionalOnMissingBean(name = "messageBundleAwareResourceResolver")
    @Bean
    public AuditResourceResolver messageBundleAwareResourceResolver() {
        return new MessageBundleAwareResourceResolver(applicationContext);
    }

    @ConditionalOnMissingBean(name = "auditPrincipalIdProvider")
    @Bean
    public AuditPrincipalIdProvider auditPrincipalIdProvider() {
        val resolvers = applicationContext.getBeansOfType(AuditPrincipalIdProvider.class, false, true);
        val providers = new ArrayList<AuditPrincipalIdProvider>(resolvers.values());
        AnnotationAwareOrderComparator.sortIfNecessary(providers);
        return new ChainingAuditPrincipalIdProvider(providers);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAuditTrailExecutionPlanConfigurer")
    @ConditionalOnProperty(prefix = "cas.audit.slf4j", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditTrailExecutionPlanConfigurer casAuditTrailExecutionPlanConfigurer() {
        return plan -> {
            val audit = casProperties.getAudit().getSlf4j();
            val slf4j = new Slf4jLoggingAuditTrailManager();
            slf4j.setUseSingleLine(audit.isUseSingleLine());
            slf4j.setEntrySeparator(audit.getSinglelineSeparator());
            slf4j.setAuditFormat(AbstractStringAuditTrailManager.AuditFormats.valueOf(audit.getAuditFormat().toUpperCase()));
            plan.registerAuditTrailManager(slf4j);
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

    private void addAuditCustomResolvers(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditActionResolvers(customAuditActionResolverMap());
        plan.registerAuditResourceResolvers(customAuditResourceResolverMap());
    }

    private void addAuditActionResolvers(final AuditTrailRecordResolutionPlan plan) {
        val resolver = authenticationActionResolver();
        plan.registerAuditActionResolver("AUTHENTICATION_RESOLVER", resolver);
        plan.registerAuditActionResolver("SAVE_SERVICE_ACTION_RESOLVER", resolver);
        plan.registerAuditActionResolver("DELETE_SERVICE_ACTION_RESOLVER",
            new ObjectCreationAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));

        val defResolver = new DefaultAuditActionResolver();
        plan.registerAuditActionResolver("DESTROY_TICKET_GRANTING_TICKET_RESOLVER", defResolver);
        plan.registerAuditActionResolver("DESTROY_PROXY_GRANTING_TICKET_RESOLVER", defResolver);

        val cResolver = ticketCreationActionResolver();
        plan.registerAuditActionResolver("CREATE_PROXY_GRANTING_TICKET_RESOLVER", cResolver);
        plan.registerAuditActionResolver("GRANT_SERVICE_TICKET_RESOLVER", cResolver);
        plan.registerAuditActionResolver("GRANT_PROXY_TICKET_RESOLVER", cResolver);
        plan.registerAuditActionResolver("CREATE_TICKET_GRANTING_TICKET_RESOLVER", cResolver);

        val authResolver = new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY);
        plan.registerAuditActionResolver("AUTHENTICATION_EVENT_ACTION_RESOLVER", authResolver);
        plan.registerAuditActionResolver("VALIDATE_SERVICE_TICKET_RESOLVER", ticketValidationActionResolver());

        val serviceAccessResolver = new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY);
        plan.registerAuditActionResolver("SERVICE_ACCESS_ENFORCEMENT_ACTION_RESOLVER", serviceAccessResolver);
    }

    private void addAuditResourceResolvers(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditResourceResolver("AUTHENTICATION_RESOURCE_RESOLVER", new CredentialsAsFirstParameterResourceResolver());
        plan.registerAuditResourceResolver("AUTHENTICATION_EVENT_RESOURCE_RESOLVER", nullableReturnValueResourceResolver());

        val messageBundleAwareResourceResolver = messageBundleAwareResourceResolver();
        plan.registerAuditResourceResolver("CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER", messageBundleAwareResourceResolver);
        plan.registerAuditResourceResolver("CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER", messageBundleAwareResourceResolver);

        val ticketResourceResolver = ticketResourceResolver();
        plan.registerAuditResourceResolver("DESTROY_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER", ticketResourceResolver);
        plan.registerAuditResourceResolver("DESTROY_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER", ticketResourceResolver);

        plan.registerAuditResourceResolver("GRANT_SERVICE_TICKET_RESOURCE_RESOLVER", new ServiceResourceResolver());
        plan.registerAuditResourceResolver("GRANT_PROXY_TICKET_RESOURCE_RESOLVER", new ServiceResourceResolver());
        plan.registerAuditResourceResolver("VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER", ticketValidationResourceResolver());

        plan.registerAuditResourceResolver("SAVE_SERVICE_RESOURCE_RESOLVER", returnValueResourceResolver());
        plan.registerAuditResourceResolver("DELETE_SERVICE_RESOURCE_RESOLVER", returnValueResourceResolver());
        plan.registerAuditResourceResolver("SERVICE_ACCESS_ENFORCEMENT_RESOURCE_RESOLVER", serviceAccessEnforcementAuditResourceResolver());
    }
}
