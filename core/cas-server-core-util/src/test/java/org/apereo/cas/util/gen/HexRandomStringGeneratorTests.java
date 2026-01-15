package org.apereo.cas.util.gen;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HexRandomStringGenerator}.
 *
 * @author Timur Duehr
 * @since 5.2.0
 */
@Tag("Simple")
class HexRandomStringGeneratorTests {

    private static final int LENGTH = 36;

    private final RandomStringGenerator randomStringGenerator = new HexRandomStringGenerator(LENGTH);

    @Test
    void verifyDefaultLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getDefaultLength());
        assertEquals(LENGTH, new HexRandomStringGenerator().getDefaultLength());
    }

    @Test
    void verifyRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(), this.randomStringGenerator.getNewString());
    }
}
