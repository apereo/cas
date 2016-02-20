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

import javax.annotation.Resource;
import java.util.Map;

/**
 * This is {@link CasAuditTrailConfiguration} that configures relevant audit trail beans.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casAuditTrailConfiguration")
public class CasAuditTrailConfiguration {
    @Value("${cas.audit.appcode:CAS}")
    private String appCode;

    @Autowired
    @Qualifier("auditablePrincipalResolver")
    private PrincipalResolver principalResolver;

    @Autowired
    @Qualifier("auditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Resource(name = "auditResourceResolverMap")
    private Map auditResourceResolverMap;

    @Resource(name = "auditActionResolverMap")
    private Map auditActionResolverMap;

    @Value("${cas.audit.singleline.separator:|}")
    private String entrySeparator;

    @Value("${cas.audit.singleline:false}")
    private boolean useSingleLine;

    @Bean(name = "auditTrailManagementAspect")
    public AuditTrailManagementAspect auditTrailManagementAspect() {
        return new AuditTrailManagementAspect(this.appCode,
                this.principalResolver, ImmutableList.of(this.auditTrailManager), auditActionResolverMap,
                auditResourceResolverMap);

    }

    @Bean(name = "auditTrailManager")
    public AuditTrailManager auditTrailManager() {
        final Slf4jLoggingAuditTrailManager mgmr = new Slf4jLoggingAuditTrailManager();
        mgmr.setUseSingleLine(this.useSingleLine);
        mgmr.setEntrySeparator(this.entrySeparator);
        return mgmr;
    }

    @Bean(name = "authenticationActionResolver")
    public DefaultAuditActionResolver authenticationActionResolver() {
        return new DefaultAuditActionResolver("_SUCCESS", "_FAILED");
    }

    @Bean(name = "ticketCreationActionResolver")
    public DefaultAuditActionResolver ticketCreationActionResolver() {
        return new DefaultAuditActionResolver("_CREATED", "_NOT_CREATED");
    }

    @Bean(name = "ticketValidationActionResolver")
    public DefaultAuditActionResolver ticketValidationActionResolver() {
        return new DefaultAuditActionResolver("D", "_FAILED");
    }

    @Bean(name = "returnValueResourceResolver")
    public ReturnValueAsStringResourceResolver returnValueResourceResolver() {
        return new ReturnValueAsStringResourceResolver();
    }
}

