package org.jasig.cas;

import org.jasig.cas.util.BinaryCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultTicketCipherExecutor} that handles the encryption
 * and signing of tickets during replication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("defaultTicketCipherExecutor")
public class DefaultTicketCipherExecutor extends BinaryCipherExecutor {

    /**
     * Instantiates a new Default ticket cipher executor.
     *
     * @param encryptionSecretKey the encryption secret key
     * @param signingSecretKey    the signing secret key
     * @param secretKeyAlg        the secret key alg
     */
    @Autowired
    public DefaultTicketCipherExecutor(@Value("${ticket.encryption.secretkey:}")
                                       final String encryptionSecretKey,
                                       @Value("${ticket.signing.secretkey:}")
                                       final String signingSecretKey,
                                       @Value("${ticket.secretkey.alg:AES}")
                                       final String secretKeyAlg) {
        super(encryptionSecretKey, signingSecretKey);
        setSecretKeyAlgorithm(secretKeyAlg);
    }
}
