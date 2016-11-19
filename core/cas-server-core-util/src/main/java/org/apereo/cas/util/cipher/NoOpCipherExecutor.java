package org.apereo.cas.util.cipher;

import java.io.Serializable;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class NoOpCipherExecutor extends AbstractCipherExecutor<Serializable, String> {
    
    @Override
    public String encode(final Serializable value) {
        return value.toString();
    }

    @Override
    public String decode(final Serializable value) {
        return value.toString();
    }
}
