package org.jasig.cas.util;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * This is a {@link PseudoTransactionManager}, borrowed from spring-integration-core
 * that serves as a mock NOOP transaction manager.
 * An implementation of {@link org.springframework.transaction.PlatformTransactionManager} 
 * that provides transaction-like semantics to
 * sources that are not inherently transactional. It does <b>not</b> make such
 * sources transactional; rather, it provides
 * the ability to synchronize operations after a flow completes, via beforeCommit, afterCommit and
 * afterRollback operations.
 *
 * @author Misagh Moayyed
 * @since 4.2.4
 */
public class PseudoTransactionManager extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new Object();
    }

    @Override
    protected void doBegin(final Object transaction, final TransactionDefinition definition)
            throws TransactionException {
        //noop
    }

    @Override
    protected void doCommit(final DefaultTransactionStatus status) throws TransactionException {
        //noop
    }

    @Override
    protected void doRollback(final DefaultTransactionStatus status) throws TransactionException {
        //noop
    }
}
