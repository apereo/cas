package org.apereo.cas.token.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link TokenTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TokenTicketCipherExecutor extends BaseStringCipherExecutor {
    public TokenTicketCipherExecutor(final String secretKeyEncryption,
                                     final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }


    @Override
    public String getName() {
        return "Tokened Tickets";
    }
}
