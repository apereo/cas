package org.apereo.cas.util.cipher;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@Getter
@NoArgsConstructor
public class NoOpCipherExecutor extends AbstractCipherExecutor<Serializable, Serializable> {

    private static CipherExecutor<Serializable, Serializable> INSTANCE;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static CipherExecutor getInstance() {
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
}
