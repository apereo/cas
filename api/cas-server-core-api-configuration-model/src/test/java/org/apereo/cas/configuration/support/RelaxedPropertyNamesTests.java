package org.apereo.cas.configuration.support;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RelaxedPropertyNamesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("CasConfiguration")
public class RelaxedPropertyNamesTests {

    @Test
    public void verifyValues() {
        val names = RelaxedPropertyNames.forCamelCase("casProperties");
        assertNotNull(names.getValues());
        assertTrue(names.iterator().hasNext());

        val names2 = RelaxedPropertyNames.forCamelCase("cas-properties");
        assertNotNull(names2.getValues());
        assertTrue(names2.iterator().hasNext());
    }

    @Test
    public void verifyTransforms() {
        Arrays.stream(RelaxedPropertyNames.Manipulation.values())
            .forEach(mani -> assertEquals(StringUtils.EMPTY, mani.apply(StringUtils.EMPTY)));

        assertEquals("cas_properties", RelaxedPropertyNames.Manipulation.CAMELCASE_TO_UNDERSCORE.apply("casProperties"));
        assertEquals("cas-properties", RelaxedPropertyNames.Manipulation.CAMELCASE_TO_HYPHEN.apply("casProperties"));
        assertEquals("cas_properties", RelaxedPropertyNames.Manipulation.PERIOD_TO_UNDERSCORE.apply("cas.properties"));
        assertEquals("cas_properties", RelaxedPropertyNames.Manipulation.NONE.apply("cas_properties"));
        assertEquals("cas_properties", RelaxedPropertyNames.Manipulation.HYPHEN_TO_UNDERSCORE.apply("cas-properties"));
        assertEquals("cas.properties", RelaxedPropertyNames.Manipulation.UNDERSCORE_TO_PERIOD.apply("cas_properties"));
    }
}
