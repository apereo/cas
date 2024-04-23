package org.apereo.cas.web;

import lombok.val;
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
    void verifyOperation() throws Throwable {
        val validator = SimpleUrlValidator.getInstance();
        assertTrue(validator.isValid("https://github.com"));
        assertTrue(validator.isValidDomain("www.github.com"));
    }
}
