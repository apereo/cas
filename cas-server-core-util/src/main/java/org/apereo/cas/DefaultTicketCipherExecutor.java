package org.apereo.cas;

import org.apereo.cas.util.BinaryCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultTicketCipherExecutor} that handles the encryption
 * and signing of tickets during replication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultTicketCipherExecutor")
public class DefaultTicketCipherExecutor extends BinaryCipherExecutor {

    @Autowired
    public DefaultTicketCipherExecutor(@Value("${ticket.encryption.secretkey:}")
                                       final String encryptionSecretKey,
                                       @Value("${ticket.signing.secretkey:}")
                                       final String signingSecretKey,
                                       @Value("${ticket.secretkey.alg:AES}")
                                       final String secretKeyAlg,
                                       @Value("${ticket.signing.key.size:512}")
                                       final int signingKeySize,
                                       @Value("${ticket.encryption.key.size:16}")
                                       final int encryptionKeySize) {
        super(encryptionSecretKey, signingSecretKey, signingKeySize, encryptionKeySize);
        setSecretKeyAlgorithm(secretKeyAlg);
    }
}
