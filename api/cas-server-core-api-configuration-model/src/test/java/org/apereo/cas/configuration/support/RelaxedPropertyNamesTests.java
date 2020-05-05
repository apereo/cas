package org.apereo.cas.configuration.support;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RelaxedPropertyNamesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class RelaxedPropertyNamesTests {

    @Test
    public void verifyOperation() {
        val names = RelaxedPropertyNames.forCamelCase("casProperties");
        assertNotNull(names.getValues());
        assertTrue(names.iterator().hasNext());
    }
}
