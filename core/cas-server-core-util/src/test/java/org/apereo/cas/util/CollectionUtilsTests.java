package org.apereo.cas.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link CollectionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CollectionUtilsTests {
    @Test
    public void verifyWrappingItemsAsList() throws Exception {
        assertEquals(CollectionUtils.wrapList(1, 2, 3, 4).size(), 4);
    }

    @Test
    public void verifyWrappingColItemsAsList() throws Exception {
        assertEquals(CollectionUtils.wrapList(new Object[]{1, 2, 3, 4}, new Object[]{1, 2, 3, 4}, 5, 6).size(), 10);
    }

    @Test
    public void verifyWrappingMapItemsAsList() throws Exception {
        assertEquals(CollectionUtils.wrapList(CollectionUtils.wrap("1", 2, "2", 2)).size(), 2);
    }
}
