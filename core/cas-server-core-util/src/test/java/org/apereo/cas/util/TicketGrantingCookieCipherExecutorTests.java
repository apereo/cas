package org.apereo.cas.util;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseStringCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class TicketGrantingCookieCipherExecutorTests {

    @Test
    public void checkEncryptionWithDefaultSettings() {
        final CipherExecutor cipherExecutor =
                new TicketGrantingCookieCipherExecutor("1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
                        "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");
        assertEquals(cipherExecutor.decode(cipherExecutor.encode("CAS Test")), "CAS Test");
    }
}
