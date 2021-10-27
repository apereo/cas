package org.jasig.cas.util;

import org.jasig.cas.CipherExecutor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link ShiroCipherExecutor}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ShiroCipherExecutorTests {

    @Test
    public void checkEncodingDecoding() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new ShiroCipherExecutor("1234567890123456",
                "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");
        final byte[] bytes = cc.encode(value.getBytes());
        final byte[] decoded = cc.decode(bytes);
        assertEquals(new String(decoded), value);
    }

    @Test(expected=RuntimeException.class)
    public void checkEncodingDecodingBadKeys() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new ShiroCipherExecutor("0000", "1234");
        cc.encode(value.getBytes());
    }
}
