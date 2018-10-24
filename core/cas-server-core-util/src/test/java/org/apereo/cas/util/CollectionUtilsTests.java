package org.apereo.cas.util;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link CollectionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CollectionUtilsTests {
    @Test
    public void verifyWrappingItemsAsList() {
        assertEquals(4, CollectionUtils.wrapList(1, 2, 3, 4).size());
    }

    @Test
    public void verifyWrappingColItemsAsList() {
        assertEquals(10, CollectionUtils.wrapList(new Object[]{1, 2, 3, 4}, new Object[]{1, 2, 3, 4}, 5, 6).size());
    }

    @Test
    public void verifyWrappingMapItemsAsList() {
        assertEquals(2, CollectionUtils.wrapList(CollectionUtils.wrap("1", 2, "2", 2)).size());
    }
}
