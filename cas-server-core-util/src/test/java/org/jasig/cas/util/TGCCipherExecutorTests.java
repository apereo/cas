package org.jasig.cas.util;

import org.jasig.cas.CipherExecutor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseStringCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class TGCCipherExecutorTests {

    @Test
    public void checkEncryptionWithDefaultSettings() {
        final CipherExecutor cipherExecutor = new TGCCipherExecutor("1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
                "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");
        assertEquals(cipherExecutor.decode(cipherExecutor.encode("CAS Test")), "CAS Test");
    }
}
