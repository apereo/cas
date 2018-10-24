package org.apereo.cas.util.gen;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultLongNumericGeneratorTests {

    @Test
    public void verifyWrap() {
        assertEquals(Long.MAX_VALUE, new DefaultLongNumericGenerator(Long.MAX_VALUE)
            .getNextLong());
    }

    @Test
    public void verifyInitialValue() {
        assertEquals(10L, new DefaultLongNumericGenerator(10L)
            .getNextLong());
    }

    @Test
    public void verifyIncrementWithNoWrap() {
        assertEquals(0, new DefaultLongNumericGenerator().getNextLong());
    }

    @Test
    public void verifyIncrementWithNoWrap2() {
        val g = new DefaultLongNumericGenerator();
        g.getNextLong();
        assertEquals(1, g.getNextLong());
    }

    @Test
    public void verifyMinimumSize() {
        assertEquals(1, new DefaultLongNumericGenerator().minLength());
    }

    @Test
    public void verifyMaximumLength() {
        assertEquals(Long.toString(Long.MAX_VALUE).length(),
            new DefaultLongNumericGenerator().maxLength());
    }
}
