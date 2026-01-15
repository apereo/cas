package org.apereo.cas.authentication.principal.merger;

import module java.base;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractAttributeMergerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
abstract class AbstractAttributeMergerTests {

    @Test
    void testNullToModify() {
        assertThrows(NullPointerException.class, () -> getAttributeMerger().mergeAttributes(null, new HashMap<>()));
    }

    @Test
    void testNullToConsider() {
        assertThrows(NullPointerException.class, () -> getAttributeMerger().mergeAttributes(new HashMap<>(), null));
    }

    protected abstract AttributeMerger getAttributeMerger();

}
