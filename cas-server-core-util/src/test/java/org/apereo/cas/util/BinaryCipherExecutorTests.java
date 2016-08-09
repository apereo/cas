package org.apereo.cas.util;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.cipher.BinaryCipherExecutor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BinaryCipherExecutor}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class BinaryCipherExecutorTests {

    @Test
    public void checkEncodingDecoding() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new BinaryCipherExecutor("1234567890123456",
                "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w", 512, 16);
        final byte[] bytes = cc.encode(value.getBytes());
        final byte[] decoded = cc.decode(bytes);
        assertEquals(new String(decoded), value);
    }

    @Test(expected=RuntimeException.class)
    public void checkEncodingDecodingBadKeys() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new BinaryCipherExecutor("0000", "1234", 512, 16);
        cc.encode(value.getBytes());
    }
}
