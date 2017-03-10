package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;

import java.io.Serializable;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class NoOpCipherExecutor extends AbstractCipherExecutor<Serializable, String> {

    private static CipherExecutor<Serializable, String> INSTANCE;

    protected NoOpCipherExecutor() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static CipherExecutor<Serializable, String> getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoOpCipherExecutor();
        }
        return INSTANCE;
    }

    @Override
    public String encode(final Serializable value) {
        return value.toString();
    }

    @Override
    public String decode(final Serializable value) {
        return value.toString();
    }

    @Override
    public String getName() {
        return null;
    }
}
