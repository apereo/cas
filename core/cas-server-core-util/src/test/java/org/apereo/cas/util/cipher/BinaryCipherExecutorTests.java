package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseBinaryCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class BinaryCipherExecutorTests {
    private static final String TEST_VALUE = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkEncodingDecoding() {
        final CipherExecutor<byte[], byte[]> cc = new TestBinaryCipherExecutor("MTIzNDU2Nzg5MDEyMzQ1Ng==",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w",
            512,
            16);
        val bytes = cc.encode(TEST_VALUE.getBytes(StandardCharsets.UTF_8), ArrayUtils.EMPTY_OBJECT_ARRAY);
        val decoded = cc.decode(bytes, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertEquals(TEST_VALUE, new String(decoded, StandardCharsets.UTF_8));
    }

    @Test
    public void checkEncodingDecodingBadKeys() {
        final CipherExecutor<byte[], byte[]> cc = new TestBinaryCipherExecutor("0000",
            "1234", 512, 16) {
        };

        this.thrown.expect(InvalidKeyException.class);
        cc.encode(TEST_VALUE.getBytes(StandardCharsets.UTF_8), ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Test
    public void checkLegacyKeys() {
        final CipherExecutor<byte[], byte[]> cc = new TestBinaryCipherExecutor("1234567890123456",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w",
            512,
            16);
        val bytes = cc.encode(TEST_VALUE.getBytes(StandardCharsets.UTF_8), ArrayUtils.EMPTY_OBJECT_ARRAY);
        val decoded = cc.decode(bytes, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertEquals(TEST_VALUE, new String(decoded, StandardCharsets.UTF_8));
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
