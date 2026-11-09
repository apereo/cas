package org.apereo.cas.token.cipher;

import module java.base;
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
@Tag("Cipher")
class JwtTicketCipherExecutorTests {

    public static final String ST = "ST-1234567890";

    @Test
    void verifyCipheredToken() {
        val c = new JwtTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", true, 0, 0);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    void verifyCipheredTokenWithoutEncryption() {
        val c = new JwtTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", false, 0, 0);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    void verifyTokenWithForeignOrNoAlgorithmIsRefused() {
        val c = new JwtTicketCipherExecutor(null,
            "qeALfMKRSME3mkHy0Qis6mhbGQFzps0ZiU-qyjsPOq_tYyR4fk2uAQR3wZfYTAlGGO3yhpJAMsq2JufeEC4fQg", false, 0, 0);
        val token = c.encode(ST);
        val encoder = Base64.getUrlEncoder().withoutPadding();
        val payload = encoder.encodeToString(ST.getBytes(StandardCharsets.UTF_8));
        val foreignAlgorithm = encoder.encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8)) + '.' + payload + ".AAAA";
        val noAlgorithm = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8)) + '.' + payload + '.';
        val badSignature = token.substring(0, token.lastIndexOf('.') + 1) + "AAAA";
        assertNull(c.decode(foreignAlgorithm));
        assertNull(c.decode(noAlgorithm));
        assertNull(c.decode(badSignature));

        c.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.SIGN_AND_ENCRYPT);
        assertThrows(NullPointerException.class, () -> c.decode(foreignAlgorithm));
        assertThrows(NullPointerException.class, () -> c.decode(noAlgorithm));
        assertEquals(ST, c.decode(c.encode(ST)));
    }

    @Test
    void verifyCipheredTokenWithoutEncryptionAndSigning() {
        val c = new JwtTicketCipherExecutor();
        c.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.ENCRYPT_AND_SIGN);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }

    @Test
    void verifyCipheredTokenWithSignAndEncrypt() {
        val c = new JwtTicketCipherExecutor(null, null, true, 0, 0);
        c.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.SIGN_AND_ENCRYPT);
        val token = c.encode(ST);
        assertEquals(ST, c.decode(token));
    }
}
