package org.apereo.cas.web;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SimpleUrlValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Web")
class SimpleUrlValidatorTests {
    @Test
    void verifyOperation() {
        val validator = SimpleUrlValidator.getInstance();
        assertTrue(validator.isValid("https://github.com"));
        assertTrue(validator.isValidDomain("www.github.com"));
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(StringUtils.EMPTY));
    }
}
