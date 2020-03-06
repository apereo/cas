package org.apereo.cas.audit.spi;

import org.apereo.cas.util.RegexUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link FilterAndDelegateAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class FilterAndDelegateAuditTrailManager implements AuditTrailManager {
    private final Collection<AuditTrailManager> auditTrailManagers;

    private final List<String> supportedActionsPerformed;

    private final List<String> excludedActionsPerformed;

    @Override
    public void record(final AuditActionContext auditActionContext) {
        var matched = supportedActionsPerformed
            .stream()
            .anyMatch(action -> {
                val actionPerformed = auditActionContext.getActionPerformed();
                return "*".equals(action) || RegexUtils.find(action, actionPerformed);
            });
        if (matched) {
            matched = excludedActionsPerformed
                .stream()
                .noneMatch(action -> {
                    val actionPerformed = auditActionContext.getActionPerformed();
                    return "*".equals(action) || RegexUtils.find(action, actionPerformed);
                });
        }
        if (matched) {
            LOGGER.trace("Recording audit action context [{}]", auditActionContext);
            auditTrailManagers.forEach(mgr -> mgr.record(auditActionContext));
        } else {
            LOGGER.trace("Skipping to record audit action context [{}] as it's not authorized as an audit action among [{}]",
                auditActionContext, supportedActionsPerformed);
        }
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        return auditTrailManagers
            .stream()
            .map(mgr -> mgr.getAuditRecordsSince(localDate))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public void removeAll() {
        auditTrailManagers.forEach(AuditTrailManager::removeAll);
    }
}
