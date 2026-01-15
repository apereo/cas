package org.apereo.cas.util;

import module java.base;
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
class SystemUtilsTests {

    @Test
    void verifyOperation() {
        val info = SystemUtils.getSystemInfo();
        assertNotNull(info);
        assertFalse(info.isEmpty());
    }
}
