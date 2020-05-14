package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServicePropertyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DefaultRegisteredServicePropertyTests {

    @Test
    public void verifyOperation() {
        val prop = new DefaultRegisteredServiceProperty(List.of("p1", "p2"));
        prop.setValues(null);
        assertFalse(prop.contains("p2"));
        assertNull(prop.getValue());
    }
}
