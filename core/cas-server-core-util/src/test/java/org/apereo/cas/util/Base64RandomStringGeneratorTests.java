package org.apereo.cas.util;

import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link Base64RandomStringGenerator}.
 *
 * @author Timur Duehr
 *
 * @since 5.2.0
 */
public class Base64RandomStringGeneratorTests {

    private static final int LENGTH = 36;

    private final RandomStringGenerator randomStringGenerator = new Base64RandomStringGenerator(
        LENGTH);

    @Test
    public void verifyDefaultLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getDefaultLength());
        assertEquals(LENGTH, new Base64RandomStringGenerator().getDefaultLength());
    }

    @Test
    public void verifyRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(),
            this.randomStringGenerator.getNewString());
    }
}
