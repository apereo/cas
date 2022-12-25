package org.apereo.cas.authentication;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PseudoPlatformTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PseudoPlatformTransactionManager implements PlatformTransactionManager, Serializable {
    @Serial
    private static final long serialVersionUID = -3501861804821200893L;

    @Nonnull
    @Override
    public TransactionStatus getTransaction(final TransactionDefinition transactionDefinition) throws TransactionException {
        return new DefaultTransactionStatus(new Object(), true, true, false, false, new Object());
    }

    @Override
    public void commit(
        @Nonnull final TransactionStatus transactionStatus) throws TransactionException {
    }

    @Override
    public void rollback(
        @Nonnull final TransactionStatus transactionStatus) throws TransactionException {
    }
}
