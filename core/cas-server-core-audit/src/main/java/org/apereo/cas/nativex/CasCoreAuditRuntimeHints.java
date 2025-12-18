package org.apereo.cas.nativex;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.inspektr.audit.AuditTrailManagementAspect;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.apereo.inspektr.common.web.ClientInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import java.util.List;

/**
 * This is {@link CasCoreAuditRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreAuditRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, List.of(AuditTrailExecutionPlanConfigurer.class));
        registerSerializationHints(hints, ClientInfo.class);
        
        registerReflectionHints(hints, List.of(
            AuditTrailExecutionPlan.class,
            AuditTrailExecutionPlanConfigurer.class,
            AuditTrailRecordResolutionPlanConfigurer.class,
            AuditTrailManagementAspect.class
        ));

        registerProxyHints(hints, List.of(
            AuditTrailManager.class,
            PrincipalResolver.class,
            AuditActionResolver.class,
            AuditResourceResolver.class,
            AuditTrailExecutionPlanConfigurer.class,
            AuditTrailRecordResolutionPlanConfigurer.class,
            AuditEventRepository.class));
    }
}
