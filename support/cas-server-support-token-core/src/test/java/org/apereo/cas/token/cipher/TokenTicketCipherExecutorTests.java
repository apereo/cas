package org.apereo.cas.token.cipher;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link TokenTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class TokenTicketCipherExecutorTests {

    public static final String ST = "ST-1234567890";

    @Test
    public void verifyCipheredToken() {
        val c = new TokenTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", true, 0, 0);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithoutEncryption() {
        val c = new TokenTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", false, 0, 0);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithoutEncryptionAndSigning() {
        val c = new TokenTicketCipherExecutor();
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }
}
