package org.apereo.cas.util.lock;

import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.PassThruLockRegistry;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * This is {@link LockRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface LockRepository {
    /**
     * Bean implementation name.
     */
    String BEAN_NAME = "casTicketRegistryLockRepository";

    /**
     * Length of the masked array,
     * which should be power of 2 - 1.
     */
    int DEFAULT_MASK_ARRAY_LENGTH = 1023;

    /**
     * No op lock repository.
     *
     * @return the default lock repository
     */
    static DefaultLockRepository noOp() {
        return new DefaultLockRepository(new PassThruLockRegistry());
    }

    /**
     * As default lock repository, uses Masked Hashcode algorithm to obtain locks.
     * When an instance of this class is created and array of Lock objects is
     * created. The length of the array is based on the 'mask'
     * parameter passed in the constructor
     *
     * @return the default lock repository
     */
    static DefaultLockRepository asDefault() {
        return new DefaultLockRepository(new DefaultLockRegistry(DEFAULT_MASK_ARRAY_LENGTH));
    }

    /**
     * Obtain a lock instance, attempt to lock on it,
     * execute the required action, and the finally unlock the lock.
     *
     * @param <T>      the type parameter
     * @param lockKey  the lock key
     * @param consumer the consumer
     * @return the optional type
     */
    <T> Optional<T> execute(Object lockKey, Supplier<T> consumer);
}
