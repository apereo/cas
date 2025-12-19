package org.apereo.cas.configuration.support;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BeansTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
class BeansTests {

    @Test
    void verifyDurations() {
        assertNotNull(Beans.newDuration("0"));
        assertNotNull(Beans.newDuration("never"));
        assertNotNull(Beans.newDuration("infinite"));
        assertNotNull(Beans.newDuration("-1"));
    }
}
