package org.apereo.cas.token.cipher;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link TokenTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class TokenTicketCipherExecutorTests {

    public static final String ST = "ST-1234567890";

    @Test
    public void verifyCipheredToken() {
        final var c = new TokenTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", true);
        final var token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithoutEncryption() {
        final var c = new TokenTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", false);
        final var token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithoutEncryptionAndSigning() {
        final var c = new TokenTicketCipherExecutor();
        final var token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }
}
