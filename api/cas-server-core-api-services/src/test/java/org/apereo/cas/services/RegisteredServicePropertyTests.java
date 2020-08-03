package org.apereo.cas.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicePropertyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class RegisteredServicePropertyTests {

    @Test
    public void verifyNull() {
        val p1 = new DefaultRegisteredServiceProperty(null);
        assertNull(p1.getValue(String.class));
        assertFalse(p1.getBooleanValue());
    }

    @Test
    public void verifyValue() {
        val p1 = new DefaultRegisteredServiceProperty("true");
        assertEquals("true", p1.getValue(String.class));
        assertTrue(p1.getBooleanValue());
    }

    @Getter
    @RequiredArgsConstructor
    private static class DefaultRegisteredServiceProperty implements RegisteredServiceProperty {
        private static final long serialVersionUID = -4878764188998002053L;

        private final String value;

        @Override
        public Set<String> getValues() {
            return Set.of();
        }

        @Override
        public boolean contains(final String value) {
            return false;
        }
    }
}
