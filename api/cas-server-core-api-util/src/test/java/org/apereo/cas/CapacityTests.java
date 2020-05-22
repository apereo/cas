package org.apereo.cas;

import org.apereo.cas.util.model.Capacity;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CapacityTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CapacityTests {
    @Test
    public void verifyOperation() {
        verify("1B", "1b", "1 b", "100 B", "12.5mb", "123.564 GB", "66.66Kb", "43.12 TB");
    }

    private static void verify(final String... values) {
        Arrays.stream(values).forEach(v -> assertNotNull(Capacity.parse(v)));
    }
}
