package org.jasig.cas.util;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class DefaultRandomStringGeneratorTests {

    private static final int LENGTH = 35;

    private final RandomStringGenerator randomStringGenerator = new DefaultRandomStringGenerator(
        LENGTH);

    @Test
    public void verifyMaxLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getMaxLength());
    }

    @Test
    public void verifyMinLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getMinLength());
    }

    @Test
    public void verifyRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(),
            this.randomStringGenerator.getNewString());
    }
}
