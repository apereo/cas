package org.apereo.cas.audit.spi;

import module java.base;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link AbstractAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractAuditTrailManager implements AuditTrailManager, DisposableBean {
    /**
     * Default maximum number of audit records to fetch.
     */
    public static final long DEFAULT_MAX_AUDIT_RECORDS_TO_FETCH = 100;

    protected boolean asynchronous;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void record(final AuditActionContext audit) {
        if (this.asynchronous) {
            executorService.execute(() -> saveAuditRecord(audit));
        } else {
            saveAuditRecord(audit);
        }
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }

    protected abstract void saveAuditRecord(AuditActionContext audit);
}
