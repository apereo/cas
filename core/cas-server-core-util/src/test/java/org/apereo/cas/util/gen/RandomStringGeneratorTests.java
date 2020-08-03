package org.apereo.cas.util.gen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Base64RandomStringGenerator}.
 *
 * @author Timur Duehr
 * @since 5.2.0
 */
@Tag("Simple")
public class RandomStringGeneratorTests {

    private static final int LENGTH = 36;

    private final RandomStringGenerator randomStringGenerator = new Base64RandomStringGenerator(LENGTH);

    @Test
    public void verifyDefaultLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getDefaultLength());
        assertEquals(LENGTH, new Base64RandomStringGenerator().getDefaultLength());
    }

    @Test
    public void verifyRandomString() {
        val s1 = this.randomStringGenerator.getNewString();
        val s2 = this.randomStringGenerator.getNewString();
        assertNotSame(s1, s2);
    }
}
