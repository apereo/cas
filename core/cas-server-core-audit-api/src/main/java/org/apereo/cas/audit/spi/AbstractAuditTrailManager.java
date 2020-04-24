package org.apereo.cas.audit.spi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This is {@link AbstractAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractAuditTrailManager implements AuditTrailManager, DisposableBean {

    /**
     * Save records asynchronously.
     */
    protected boolean asynchronous;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, "AuditTrailManagerThread");
            }
        });

    @Override
    public void record(final AuditActionContext audit) {
        if (this.asynchronous) {
            this.executorService.execute(() -> saveAuditRecord(audit));
        } else {
            saveAuditRecord(audit);
        }
    }

    @Override
    public void destroy() {
        this.executorService.shutdown();
    }

    /**
     * Actual audit record save method.
     * @param audit Audit record to be saved.
     */
    protected abstract void saveAuditRecord(AuditActionContext audit);
}
