package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link JwtTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketCipherExecutor extends BaseStringCipherExecutor {
    public JwtTicketCipherExecutor(final String secretKeyEncryption,
                                   final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }
}
