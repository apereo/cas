package org.apereo.cas.util.cipher;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseStringCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class TicketGrantingCookieCipherExecutorTests {

    @Test
    public void verifyAction() {
        final var cipher = new TicketGrantingCookieCipherExecutor();
        final var encoded = cipher.encode("ST-1234567890");
        assertEquals("ST-1234567890", cipher.decode(encoded));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    public void checkEncryptionWithDefaultSettings() {
        final CipherExecutor cipherExecutor =
                new TicketGrantingCookieCipherExecutor("1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
                        "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");
        final var result = cipherExecutor.decode(cipherExecutor.encode("CAS Test"));
        assertEquals("CAS Test", result);
    }
}
