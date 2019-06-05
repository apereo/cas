package org.apereo.cas.audit.spi;

import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link MockAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public class MockAuditTrailManager implements AuditTrailManager {
    private final Set<AuditActionContext> auditRecords = new LinkedHashSet<>();

    @Override
    public void record(final AuditActionContext auditActionContext) {
        auditRecords.add(auditActionContext);
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        val dt = DateTimeUtils.dateOf(localDate);
        return auditRecords
            .stream()
            .filter(audit -> audit.getWhenActionWasPerformed().compareTo(dt) >= 0)
            .collect(Collectors.toSet());
    }

    @Override
    public void removeAll() {
        auditRecords.clear();
    }
}
