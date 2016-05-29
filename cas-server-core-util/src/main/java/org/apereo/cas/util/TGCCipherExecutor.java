package org.apereo.cas.util;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link TGCCipherExecutor} that reads TGC keys from the CAS config
 * and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class TGCCipherExecutor extends BaseStringCipherExecutor {

    /**
     * Instantiates a new Tgc cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     */
    @Autowired
    public TGCCipherExecutor(final String secretKeyEncryption,
                             final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }
}
