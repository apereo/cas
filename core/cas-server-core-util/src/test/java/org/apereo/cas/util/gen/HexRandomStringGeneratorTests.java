package org.apereo.cas.util.gen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HexRandomStringGenerator}.
 *
 * @author Timur Duehr
 * @since 5.2.0
 */
public class HexRandomStringGeneratorTests {

    private static final int LENGTH = 36;

    private final RandomStringGenerator randomStringGenerator = new HexRandomStringGenerator(LENGTH);

    @Test
    public void verifyDefaultLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getDefaultLength());
        assertEquals(LENGTH, new HexRandomStringGenerator().getDefaultLength());
    }

    @Test
    public void verifyRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(), this.randomStringGenerator.getNewString());
    }
}
