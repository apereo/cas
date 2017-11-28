package org.apereo.cas.authentication;

import com.google.common.collect.Multimap;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * This is {@link CoreAuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CoreAuthenticationUtilsTests {

    @Test
    public void verifyPrincipalAttributeTransformations() {
        final List<String> list = Stream.of("a1", "a2:newA2", "a1:newA1").collect(Collectors.toList());
        final Multimap<String, String> result = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(list);
        assertEquals(result.size(), 3);
        assertTrue(result.containsKey("a2"));
        assertTrue(result.containsKey("a1"));

        final Map<String, Collection<String>> map = CollectionUtils.wrap(result);
        assertEquals(map.get("a2").size(), 1);
        assertEquals(map.get("a1").size(), 2);
        assertTrue(map.get("a2").contains("newA2"));
        assertTrue(map.get("a1").contains("a1"));
        assertTrue(map.get("a1").contains("newA1"));
    }
}
