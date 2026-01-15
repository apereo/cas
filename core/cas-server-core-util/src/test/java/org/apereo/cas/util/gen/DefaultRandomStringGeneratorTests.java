package org.apereo.cas.util.gen;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
class DefaultRandomStringGeneratorTests {

    private static final int LENGTH = 36;

    private final RandomStringGenerator randomStringGenerator = new DefaultRandomStringGenerator(
        LENGTH);

    @Test
    void verifyDefaultLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getDefaultLength());
    }

    @Test
    void verifyRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(),
            this.randomStringGenerator.getNewString());
    }
}
