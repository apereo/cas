package org.jasig.cas.config;

import com.google.common.collect.ImmutableList;
import org.jasig.inspektr.audit.AuditTrailManagementAspect;
import org.jasig.inspektr.audit.AuditTrailManager;
import org.jasig.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.jasig.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.jasig.inspektr.audit.support.Slf4jLoggingAuditTrailManager;
import org.jasig.inspektr.common.spi.PrincipalResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Map;

/**
 * This is {@link CasAuditTrailConfiguration} that configures relevant audit trail beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casAuditTrailConfiguration")
@Lazy(true)
public class CasAuditTrailConfiguration {
    /**
     * The App code.
     */
    @Value("${cas.audit.appcode:CAS}")
    private String appCode;

    /**
     * The Principal resolver.
     */
    @Autowired
    @Qualifier("auditablePrincipalResolver")
    private PrincipalResolver principalResolver;
    
    /**
     * The Audit resource resolver map.
     */
    @Resource(name = "auditResourceResolverMap")
    private Map auditResourceResolverMap;

    /**
     * The Audit action resolver map.
     */
    @Resource(name = "auditActionResolverMap")
    private Map auditActionResolverMap;

    /**
     * The Entry separator.
     */
    @Value("${cas.audit.singleline.separator:|}")
    private String entrySeparator;

    /**
     * The Use single line.
     */
    @Value("${cas.audit.singleline:false}")
    private boolean useSingleLine;

    /**
     * Audit trail management aspect audit trail management aspect.
     *
     * @return the audit trail management aspect
     */
    @Bean(name = "auditTrailManagementAspect")
    public AuditTrailManagementAspect auditTrailManagementAspect() {
        return new AuditTrailManagementAspect(this.appCode,
                this.principalResolver, ImmutableList.of(auditTrailManager()), auditActionResolverMap,
                auditResourceResolverMap);

    }

    /**
     * Audit trail manager audit trail manager.
     *
     * @return the audit trail manager
     */
    @Bean(name = "slf4jAuditTrailManager")
    public AuditTrailManager auditTrailManager() {
        final Slf4jLoggingAuditTrailManager mgmr = new Slf4jLoggingAuditTrailManager();
        mgmr.setUseSingleLine(this.useSingleLine);
        mgmr.setEntrySeparator(this.entrySeparator);
        return mgmr;
    }

    /**
     * Authentication action resolver default audit action resolver.
     *
     * @return the default audit action resolver
     */
    @Bean(name = "authenticationActionResolver")
    public DefaultAuditActionResolver authenticationActionResolver() {
        return new DefaultAuditActionResolver("_SUCCESS", "_FAILED");
    }

    /**
     * Ticket creation action resolver default audit action resolver.
     *
     * @return the default audit action resolver
     */
    @Bean(name = "ticketCreationActionResolver")
    public DefaultAuditActionResolver ticketCreationActionResolver() {
        return new DefaultAuditActionResolver("_CREATED", "_NOT_CREATED");
    }

    /**
     * Ticket validation action resolver default audit action resolver.
     *
     * @return the default audit action resolver
     */
    @Bean(name = "ticketValidationActionResolver")
    public DefaultAuditActionResolver ticketValidationActionResolver() {
        return new DefaultAuditActionResolver("D", "_FAILED");
    }

    /**
     * Return value resource resolver return value as string resource resolver.
     *
     * @return the return value as string resource resolver
     */
    @Bean(name = "returnValueResourceResolver")
    public ReturnValueAsStringResourceResolver returnValueResourceResolver() {
        return new ReturnValueAsStringResourceResolver();
    }
}

