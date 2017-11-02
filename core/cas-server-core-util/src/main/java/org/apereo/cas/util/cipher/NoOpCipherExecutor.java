package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;

import java.io.Serializable;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class NoOpCipherExecutor extends AbstractCipherExecutor<Serializable, Serializable> {

    private static CipherExecutor<Serializable, Serializable> INSTANCE;

    protected NoOpCipherExecutor() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static CipherExecutor<Serializable, Serializable> getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoOpCipherExecutor();
        }
        return INSTANCE;
    }

    @Override
    public Serializable encode(final Serializable value) {
        return value;
    }

    @Override
    public Serializable decode(final Serializable value) {
        return value;
    }

    @Override
    public String getName() {
        return null;
    }
}
