package org.apereo.cas.configuration.support;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is {@link RelaxedPropertyNamesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("CasConfiguration")
public class RelaxedPropertyNamesTests {

    @ParameterizedTest
    @ValueSource(strings = {"casProperties", "cas-properties"})
    public void verifyValues(final String property) {
        val names = RelaxedPropertyNames.forCamelCase(property);
        assertNotNull(names.getValues());
        assertTrue(names.iterator().hasNext());
    }

    @ParameterizedTest
    @EnumSource
    public void verifyEmptyTransforms(final RelaxedPropertyNames.Manipulation mani) {
        assertEquals(StringUtils.EMPTY, mani.apply(StringUtils.EMPTY));
    }

    @ParameterizedTest
    @MethodSource("transformsProvider")
    public void verifyTransforms(final String expected, final RelaxedPropertyNames.Manipulation mani, final String testValue) {
        assertEquals(expected, mani.apply(testValue));
    }

    static Stream<Arguments> transformsProvider() {
        return Stream.of(
                arguments("cas_properties", RelaxedPropertyNames.Manipulation.CAMELCASE_TO_UNDERSCORE, "casProperties"),
                arguments("cas-properties", RelaxedPropertyNames.Manipulation.CAMELCASE_TO_HYPHEN, "casProperties"),
                arguments("cas_properties", RelaxedPropertyNames.Manipulation.PERIOD_TO_UNDERSCORE, "cas.properties"),
                arguments("cas_properties", RelaxedPropertyNames.Manipulation.NONE, "cas_properties"),
                arguments("cas_properties", RelaxedPropertyNames.Manipulation.HYPHEN_TO_UNDERSCORE, "cas-properties"),
                arguments("cas.properties", RelaxedPropertyNames.Manipulation.UNDERSCORE_TO_PERIOD, "cas_properties")
        );
    }
}
