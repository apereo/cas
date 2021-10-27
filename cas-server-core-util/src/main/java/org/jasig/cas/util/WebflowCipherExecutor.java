package org.jasig.cas.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is {@link WebflowCipherExecutor}, that reads webflow keys
 * from CAS configuration and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("webflowCipherExecutor")
public class WebflowCipherExecutor extends BinaryCipherExecutor {

    /**
     * Instantiates a new webflow cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     * @param secretKeyAlg        the secret key alg
     */
    @Autowired
    public WebflowCipherExecutor(@Value("${webflow.encryption.key:}")
                                 final String secretKeyEncryption,
                                 @Value("${webflow.signing.key:}")
                                 final String secretKeySigning,
                                 @Value("${webflow.secretkey.alg:AES}")
                                 final String secretKeyAlg){
        super(secretKeyEncryption, secretKeySigning);
        setSecretKeyAlgorithm(secretKeyAlg);
    }
}
