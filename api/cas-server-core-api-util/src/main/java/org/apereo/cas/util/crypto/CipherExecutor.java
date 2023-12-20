package org.apereo.cas.util.crypto;

import java.io.Serializable;
import java.security.Key;

/**
 * Responsible to define operation that deal with encryption, signing
 * and verification of a value.
 *
 * @author Misagh Moayyed
 * @param <I> the type parameter for the input
 * @param <O> the type parameter for the output
 * @since 4.1
 */
public interface CipherExecutor<I, O> extends EncodableCipher<I, O>, DecodableCipher<I, O> {
    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor Serializable -> Serializable}
     */
    static CipherExecutor<Serializable, Serializable> noOp() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor String -> String}
     */
    static CipherExecutor<String, String> noOpOfStringToString() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor Number -> Number}
     */
    static CipherExecutor<Number, Number> noOpOfNumberToNumber() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Factory method.
     *
     * @return Strongly -typed Noop {@code CipherExecutor Serializable -> String}
     */
    static CipherExecutor<Serializable, String> noOpOfSerializableToString() {
        return NoOpCipherExecutor.INSTANCE;
    }

    /**
     * Supports encryption of values.
     *
     * @return true /false
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * The (component) name of this cipher.
     *
     * @return the name.
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Produce the signing key used to sign tokens in this cipher.
     *
     * @return key instance
     */
    default Key getSigningKey() {
        return null;
    }
}
