package org.apereo.cas.util.spring;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        var results = BeanContainer.toList(List.of("one")).and("two").get();
        assertEquals(2, results.size());

        results = BeanContainer.toList("one").and("two").get();
        assertEquals(2, results.size());
    }
}
