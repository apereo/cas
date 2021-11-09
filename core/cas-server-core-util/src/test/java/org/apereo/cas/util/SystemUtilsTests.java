package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SystemUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Utility")
public class SystemUtilsTests {

    @Test
    public void verifyOperation() {
        val info = SystemUtils.getSystemInfo();
        assertNotNull(info);
        assertFalse(info.isEmpty());
    }
}
