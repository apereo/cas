package org.apereo.cas.util.cipher;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class NoOpCipherExecutor extends AbstractCipherExecutor<String, String> {
    
    @Override
    public String encode(final String value) {
        return value;
    }

    @Override
    public String decode(final String value) {
        return value;
    }
}
