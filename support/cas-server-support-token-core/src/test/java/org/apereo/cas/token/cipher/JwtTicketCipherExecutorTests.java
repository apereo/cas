package org.apereo.cas.token.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Simple")
public class JwtTicketCipherExecutorTests {

    public static final String ST = "ST-1234567890";

    @Test
    public void verifyCipheredToken() {
        val c = new JwtTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", true, 0, 0);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithoutEncryption() {
        val c = new JwtTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", false, 0, 0);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithoutEncryptionAndSigning() {
        val c = new JwtTicketCipherExecutor();
        c.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.ENCRYPT_AND_SIGN);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    public void verifyCipheredTokenWithSignAndEncrypt() {
        val c = new JwtTicketCipherExecutor(null, null, true, 0, 0);
        c.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.SIGN_AND_ENCRYPT);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }
}
