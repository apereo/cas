package org.apereo.cas.audit.spi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is {@link AbstractAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractAuditTrailManager implements AuditTrailManager {

    /**
     * Save records asynchronously.
     */
    protected boolean asynchronous;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void record(final AuditActionContext audit) {
        if (this.asynchronous) {
            this.executorService.execute(() -> saveAuditRecord(audit));
        } else {
            saveAuditRecord(audit);
        }
    }

    /**
     * Actual audit record save method.
     * @param audit Audit record to be saved.
     */
    protected abstract void saveAuditRecord(AuditActionContext audit);
}
