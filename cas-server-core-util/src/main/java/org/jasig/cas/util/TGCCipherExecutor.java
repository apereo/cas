package org.jasig.cas.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is {@link TGCCipherExecutor} that reads TGC keys from the CAS config
 * and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("tgcCipherExecutor")
public class TGCCipherExecutor extends BaseStringCipherExecutor {

    /**
     * Instantiates a new Tgc cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     */
    @Autowired
    public TGCCipherExecutor(@Value("${tgc.encryption.key:}")
                             final String secretKeyEncryption,
                             @Value("${tgc.signing.key:}")
                             final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }
}
