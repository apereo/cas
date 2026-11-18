package org.apereo.cas.util.spring;

import module java.base;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * A transaction manager that begins, commits and rolls back nothing. It serves components that require
 * a transaction manager when no transactional resource is configured.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class PseudoPlatformTransactionManager extends AbstractPlatformTransactionManager {
    @Serial
    private static final long serialVersionUID = -2730538214377421567L;

    @Override
    protected Object doGetTransaction() {
        return new Object();
    }

    @Override
    protected void doBegin(final Object transaction, final TransactionDefinition definition) {
    }

    @Override
    protected void doCommit(final DefaultTransactionStatus status) {
    }

    @Override
    protected void doRollback(final DefaultTransactionStatus status) {
    }
}
