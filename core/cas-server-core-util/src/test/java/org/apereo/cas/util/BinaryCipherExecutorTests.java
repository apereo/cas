package org.apereo.cas.util;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.cipher.BaseBinaryCipherExecutor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseBinaryCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class BinaryCipherExecutorTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkEncodingDecoding() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new TestBinaryCipherExecutor("MTIzNDU2Nzg5MDEyMzQ1Ng==",
                "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w",
                512,
                16);
        final byte[] bytes = cc.encode(value.getBytes());
        final byte[] decoded = cc.decode(bytes);
        assertEquals(new String(decoded), value);
    }

    @Test
    public void checkEncodingDecodingBadKeys() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new TestBinaryCipherExecutor("0000",
                "1234", 512, 16) {
        };

        this.thrown.expect(RuntimeException.class);
        this.thrown.expectMessage("Unable to init cipher instance.");

        cc.encode(value.getBytes());
    }

    @Test
    public void checkLegacyKeys() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new TestBinaryCipherExecutor("1234567890123456",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w",
            512,
            16);
        final byte[] bytes = cc.encode(value.getBytes());
        final byte[] decoded = cc.decode(bytes);
        assertEquals(new String(decoded), value);
    }
    private static class TestBinaryCipherExecutor extends BaseBinaryCipherExecutor {
        TestBinaryCipherExecutor(final String encKey, final String signingKey, final int sKey, final int eKey) {
            super(encKey, signingKey, sKey, eKey, "Test");
        }

        @Override
        protected String getEncryptionKeySetting() {
            return "undefined";
        }

        @Override
        protected String getSigningKeySetting() {
            return "undefined";
        }
    }
}
