package org.apereo.cas.mgmt.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.services.audit.Pac4jAuditablePrincipalResolver;
import org.apereo.cas.mgmt.services.audit.ServiceManagementResourceResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.inspektr.audit.AuditTrailManagementAspect;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.ObjectCreationAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.ParametersAsStringResourceResolver;
import org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasManagementAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casManagementAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasManagementAuditConfiguration {
    private static final String AUDIT_ACTION_SUFFIX_FAILED = "_FAILED";
    private static final String AUDIT_ACTION_SUFFIX_SUCCESS = "_SUCCESS";

    @Bean
    public AuditResourceResolver saveServiceResourceResolver() {
        return new ParametersAsStringResourceResolver();
    }

    @Bean
    public AuditResourceResolver deleteServiceResourceResolver() {
        return new ServiceManagementResourceResolver();
    }

    @Bean
    public AuditActionResolver saveServiceActionResolver() {
        return new DefaultAuditActionResolver(AUDIT_ACTION_SUFFIX_SUCCESS, AUDIT_ACTION_SUFFIX_FAILED);
    }

    @Bean
    public AuditActionResolver deleteServiceActionResolver() {
        return new ObjectCreationAuditActionResolver(AUDIT_ACTION_SUFFIX_SUCCESS, AUDIT_ACTION_SUFFIX_FAILED);
    }

    @Bean
    public PrincipalResolver auditablePrincipalResolver() {
        return new Pac4jAuditablePrincipalResolver();
    }

    @Bean
    public AuditTrailManagementAspect auditTrailManagementAspect() {
        return new AuditTrailManagementAspect("CAS_Management",
                auditablePrincipalResolver(), CollectionUtils.wrap(auditTrailManager()),
                auditActionResolverMap(),
                auditResourceResolverMap());
    }

    @Bean
    @RefreshScope
    public AuditTrailManager auditTrailManager() {
        return new Slf4jLoggingAuditTrailManager();
    }

    @Bean
    public Map<String, AuditResourceResolver> auditResourceResolverMap() {
        final Map<String, AuditResourceResolver> map = new HashMap<>(2);
        map.put("DELETE_SERVICE_RESOURCE_RESOLVER", deleteServiceResourceResolver());
        map.put("SAVE_SERVICE_RESOURCE_RESOLVER", saveServiceResourceResolver());
        return map;
    }

    @Bean
    public Map<String, AuditActionResolver> auditActionResolverMap() {
        final Map<String, AuditActionResolver> map = new HashMap<>(2);
        map.put("DELETE_SERVICE_ACTION_RESOLVER", deleteServiceActionResolver());
        map.put("SAVE_SERVICE_ACTION_RESOLVER", saveServiceActionResolver());
        return map;
    }

    @Bean
    public FilterRegistrationBean casClientInfoLoggingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ClientInfoThreadLocalFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("CAS Client Info Logging Filter");
        bean.setAsyncSupported(true);
        return bean;
    }
}
