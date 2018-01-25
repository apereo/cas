package org.apereo.cas.audit.spi.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.spi.ChainingAuditPrincipalIdProvider;
import org.apereo.cas.audit.spi.CredentialsAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.DefaultAuditTrailExecutionPlan;
import org.apereo.cas.audit.spi.DefaultAuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.spi.MessageBundleAwareResourceResolver;
import org.apereo.cas.audit.spi.NullableReturnValueAuditResourceResolver;
import org.apereo.cas.audit.spi.ServiceResourceResolver;
import org.apereo.cas.audit.spi.ShortenedReturnValueAsStringResourceResolver;
import org.apereo.cas.audit.spi.ThreadLocalPrincipalResolver;
import org.apereo.cas.audit.spi.TicketAsFirstParameterResourceResolver;
import org.apereo.cas.audit.spi.TicketValidationResourceResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.audit.AuditProperties;
import org.apereo.cas.configuration.model.core.audit.AuditSlf4jLogProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.inspektr.audit.AuditTrailManagementAspect;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;

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
@EnableAspectJAutoProxy
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuditConfiguration implements AuditTrailExecutionPlanConfigurer, AuditTrailRecordResolutionPlanConfigurer {

    private static final String AUDIT_ACTION_SUFFIX_FAILED = "_FAILED";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public AuditTrailManagementAspect auditTrailManagementAspect(@Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailManager,
                                                                 @Qualifier("auditTrailRecordResolutionPlan") final AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan) {
        final AuditTrailManagementAspect aspect = new AuditTrailManagementAspect(
            casProperties.getAudit().getAppCode(),
            auditablePrincipalResolver(auditPrincipalIdProvider()),
            auditTrailManager.getAuditTrailManagers(), auditTrailRecordResolutionPlan.getAuditActionResolvers(),
            auditTrailRecordResolutionPlan.getAuditResourceResolvers());
        aspect.setFailOnAuditFailures(!casProperties.getAudit().isIgnoreAuditFailures());
        return aspect;
    }

    @Autowired
    @ConditionalOnMissingBean(name = "auditTrailRecordResolutionPlan")
    @Bean
    public AuditTrailRecordResolutionPlan auditTrailRecordResolutionPlan(final List<AuditTrailRecordResolutionPlanConfigurer> configurers) {
        final DefaultAuditTrailRecordResolutionPlan plan = new DefaultAuditTrailRecordResolutionPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Registering audit trail manager [{}]", name);
            c.configureAuditTrailRecordResolutionPlan(plan);
        });
        return plan;
    }

    @Autowired
    @ConditionalOnMissingBean(name = "auditTrailExecutionPlan")
    @Bean
    public AuditTrailExecutionPlan auditTrailExecutionPlan(final List<AuditTrailExecutionPlanConfigurer> configurers) {
        final DefaultAuditTrailExecutionPlan plan = new DefaultAuditTrailExecutionPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Registering audit trail manager [{}]", name);
            c.configureAuditTrailExecutionPlan(plan);
        });
        return plan;
    }

    @Bean
    public FilterRegistrationBean casClientInfoLoggingFilter() {
        final AuditProperties audit = casProperties.getAudit();

        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ClientInfoThreadLocalFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("CAS Client Info Logging Filter");
        bean.setAsyncSupported(true);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        final Map<String, String> initParams = new HashMap<>();
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
        return new DefaultAuditActionResolver("_SUCCESS", AUDIT_ACTION_SUFFIX_FAILED);
    }

    @ConditionalOnMissingBean(name = "ticketCreationActionResolver")
    @Bean
    public AuditActionResolver ticketCreationActionResolver() {
        return new DefaultAuditActionResolver("_CREATED", "_NOT_CREATED");
    }

    @ConditionalOnMissingBean(name = "ticketValidationActionResolver")
    @Bean
    public AuditActionResolver ticketValidationActionResolver() {
        return new DefaultAuditActionResolver("D", AUDIT_ACTION_SUFFIX_FAILED);
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
        final AuditProperties audit = casProperties.getAudit();
        if (audit.isIncludeValidationAssertion()) {
            return new TicketValidationResourceResolver();
        }
        return ticketResourceResolver();
    }

    @ConditionalOnMissingBean(name = "messageBundleAwareResourceResolver")
    @Bean
    public AuditResourceResolver messageBundleAwareResourceResolver() {
        return new MessageBundleAwareResourceResolver();
    }

    @ConditionalOnMissingBean(name = "auditPrincipalIdProvider")
    @Bean
    public AuditPrincipalIdProvider auditPrincipalIdProvider() {
        final ChainingAuditPrincipalIdProvider chain = new ChainingAuditPrincipalIdProvider();
        final Map<String, AuditPrincipalIdProvider> resolvers = applicationContext.getBeansOfType(AuditPrincipalIdProvider.class, false, true);
        resolvers.values().forEach(chain::addProvider);
        return chain;
    }

    @Override
    public void configureAuditTrailExecutionPlan(final AuditTrailExecutionPlan plan) {
        final AuditSlf4jLogProperties audit = casProperties.getAudit().getSlf4j();
        final Slf4jLoggingAuditTrailManager slf4j = new Slf4jLoggingAuditTrailManager();
        slf4j.setUseSingleLine(audit.isUseSingleLine());
        slf4j.setEntrySeparator(audit.getSinglelineSeparator());
        slf4j.setAuditFormat(audit.getAuditFormat());
        plan.registerAuditTrailManager(slf4j);
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        /*
            Add audit action resolvers here.
         */
        final AuditActionResolver resolver = authenticationActionResolver();
        plan.registerAuditActionResolver("AUTHENTICATION_RESOLVER", resolver);
        plan.registerAuditActionResolver("SAVE_SERVICE_ACTION_RESOLVER", resolver);

        final AuditActionResolver defResolver = new DefaultAuditActionResolver();
        plan.registerAuditActionResolver("DESTROY_TICKET_GRANTING_TICKET_RESOLVER", defResolver);
        plan.registerAuditActionResolver("DESTROY_PROXY_GRANTING_TICKET_RESOLVER", defResolver);

        final AuditActionResolver cResolver = ticketCreationActionResolver();
        plan.registerAuditActionResolver("CREATE_PROXY_GRANTING_TICKET_RESOLVER", cResolver);
        plan.registerAuditActionResolver("GRANT_SERVICE_TICKET_RESOLVER", cResolver);
        plan.registerAuditActionResolver("GRANT_PROXY_TICKET_RESOLVER", cResolver);
        plan.registerAuditActionResolver("CREATE_TICKET_GRANTING_TICKET_RESOLVER", cResolver);

        final AuditActionResolver authResolver = new DefaultAuditActionResolver("_TRIGGERED", StringUtils.EMPTY);
        plan.registerAuditActionResolver("AUTHENTICATION_EVENT_ACTION_RESOLVER", authResolver);
        plan.registerAuditActionResolver("VALIDATE_SERVICE_TICKET_RESOLVER", ticketValidationActionResolver());

        /*
            Add audit resource resolvers here.
         */
        plan.registerAuditResourceResolver("AUTHENTICATION_RESOURCE_RESOLVER", new CredentialsAsFirstParameterResourceResolver());

        final AuditResourceResolver messageBundleAwareResourceResolver = messageBundleAwareResourceResolver();
        plan.registerAuditResourceResolver("CREATE_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER", messageBundleAwareResourceResolver);
        plan.registerAuditResourceResolver("CREATE_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER", messageBundleAwareResourceResolver);

        final AuditResourceResolver ticketResourceResolver = ticketResourceResolver();
        plan.registerAuditResourceResolver("DESTROY_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER", ticketResourceResolver);
        plan.registerAuditResourceResolver("DESTROY_PROXY_GRANTING_TICKET_RESOURCE_RESOLVER", ticketResourceResolver);

        plan.registerAuditResourceResolver("GRANT_SERVICE_TICKET_RESOURCE_RESOLVER", new ServiceResourceResolver());
        plan.registerAuditResourceResolver("GRANT_PROXY_TICKET_RESOURCE_RESOLVER", new ServiceResourceResolver());
        plan.registerAuditResourceResolver("VALIDATE_SERVICE_TICKET_RESOURCE_RESOLVER", ticketValidationResourceResolver());

        plan.registerAuditResourceResolver("SAVE_SERVICE_RESOURCE_RESOLVER", returnValueResourceResolver());
        plan.registerAuditResourceResolver("AUTHENTICATION_EVENT_RESOURCE_RESOLVER", nullableReturnValueResourceResolver());

        /*
            Add custom resolvers here.
         */
        plan.registerAuditActionResolvers(customAuditActionResolverMap());
        plan.registerAuditResourceResolvers(customAuditResourceResolverMap());
    }
}
