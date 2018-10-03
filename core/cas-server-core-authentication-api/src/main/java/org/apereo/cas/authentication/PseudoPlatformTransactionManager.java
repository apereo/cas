package org.apereo.cas.authentication;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.io.Serializable;

/**
 * This is {@link PseudoPlatformTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PseudoPlatformTransactionManager implements PlatformTransactionManager, Serializable {
    private static final long serialVersionUID = -3501861804821200893L;

    @Override
    public TransactionStatus getTransaction(final TransactionDefinition transactionDefinition) throws TransactionException {
        return new DefaultTransactionStatus(new Object(), true, true, false, false, new Object());
    }

    @Override
    public void commit(final TransactionStatus transactionStatus) throws TransactionException {
    }

    @Override
    public void rollback(final TransactionStatus transactionStatus) throws TransactionException {
    }
}
