package org.apereo.cas.util.crypto;

import lombok.Getter;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 * <p>
 * This singleton class is hidden from "the world" by being package-private and is exposed to consumers by
 * {@link CipherExecutor}'s static factory method.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.1
 */
@Getter
class NoOpCipherExecutor<I, O> implements CipherExecutor<I, O> {

    /**
     * Static instance.
     */
    public static final CipherExecutor INSTANCE = new NoOpCipherExecutor();

    @Override
    public O encode(final I value, final Object[] parameters) {
        return (O) value;
    }

    @Override
    public O decode(final I value, final Object[] parameters) {
        return (O) value;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
