package org.apereo.cas.authentication;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * This is {@link PseudoPlatformTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PseudoPlatformTransactionManager extends AbstractPlatformTransactionManager {
    private static final long serialVersionUID = -3501861804821200893L;

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new Object();
    }

    @Override
    protected void doBegin(final Object o, final TransactionDefinition transactionDefinition) throws TransactionException {
    }

    @Override
    protected void doCommit(final DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
    }

    @Override
    protected void doRollback(final DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
    }
}
