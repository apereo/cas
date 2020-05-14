package org.apereo.cas.util;

import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CollectionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Simple")
public class CollectionUtilsTests {
    @Test
    public void verifyToCol() {
        assertThrows(IllegalArgumentException.class, () ->
            CollectionUtils.toCollection("item", List.class));
        assertNotNull(CollectionUtils.toCollection(List.of("one").iterator()));
        assertNotNull(CollectionUtils.toCollection(Collections.enumeration(List.of("one"))));
    }

    @Test
    public void verifyWrap() {
        assertNotNull(CollectionUtils.wrap((Map) null));
        assertNotNull(CollectionUtils.wrap((Multimap) null));
        assertNotNull(CollectionUtils.wrap(List.of()));

        assertNotNull(CollectionUtils.asMultiValueMap(Map.of()));
        assertNotNull(CollectionUtils.asMultiValueMap("key", "value"));
        assertNotNull(CollectionUtils.convertDirectedListToMap(List.of("key->value")));

        assertNotNull(CollectionUtils.wrap("1", "1", "2", "2", "3", "3",
            "4", "4", "5", "5", "6", "6", "7", "7",
            "8", "8", "9", "9"));
    }

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
