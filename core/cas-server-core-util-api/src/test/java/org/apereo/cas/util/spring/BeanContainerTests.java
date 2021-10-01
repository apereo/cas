package org.apereo.cas.util.spring;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BeanContainerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
public class BeanContainerTests {
    @Test
    public void verifyOperation() {
        var results = BeanContainer.of(List.of("one")).and("two").toList();
        assertEquals(2, results.size());

        results = BeanContainer.of("one").and("two").toList();
        assertEquals(2, results.size());

        var set = BeanContainer.of("one").and("one").toSet();
        assertEquals(1, set.size());

        set = BeanContainer.of(Set.of("hello", "world")).toSet();
        assertEquals(2, set.size());

        assertEquals(1, BeanContainer.of("one").size());
    }
}
