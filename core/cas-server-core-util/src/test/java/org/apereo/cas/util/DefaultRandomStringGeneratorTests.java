package org.apereo.cas.util;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
@Slf4j
public class DefaultRandomStringGeneratorTests {

    private static final int LENGTH = 36;

    private final RandomStringGenerator randomStringGenerator = new DefaultRandomStringGenerator(
        LENGTH);

    @Test
    public void verifyDefaultLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getDefaultLength());
    }

    @Test
    public void verifyRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(),
            this.randomStringGenerator.getNewString());
    }
}
