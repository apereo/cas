package org.apereo.cas.configuration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CommaSeparatedStringToThrowablesConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("CasConfiguration")
class CommaSeparatedStringToThrowablesConverterTests {
    @Test
    void verifyConverters() {
        val converter = new CommaSeparatedStringToThrowablesConverter();
        val list = converter.convert(Exception.class.getName() + ',' + RuntimeException.class.getName());
        assertEquals(2, list.size());
    }

    @Test
    void verifyConverter() {
        val converter = new CommaSeparatedStringToThrowablesConverter();
        val list = converter.convert(Exception.class.getName());
        assertEquals(1, list.size());
    }
}
