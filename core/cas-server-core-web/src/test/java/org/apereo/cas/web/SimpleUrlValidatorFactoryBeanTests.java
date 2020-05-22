package org.apereo.cas.web;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author swoeste
 * @since 5.1.0
 */
@Tag("Simple")
public class SimpleUrlValidatorFactoryBeanTests {

    @Test
    public void verifyValidation() {
        val validator = new SimpleUrlValidatorFactoryBean(false).getObject();
        assertNotNull(validator);
        assertTrue(validator.isValid("http://www.demo.com/logout"));
        assertFalse(validator.isValid("http://localhost/logout"));
    }

    @Test
    public void verifyValidationWithLocalUrlAllowed() {
        val validator = new SimpleUrlValidatorFactoryBean(true).getObject();
        assertNotNull(validator);
        assertTrue(validator.isValid("http://www.demo.com/logout"));
        assertTrue(validator.isValid("http://localhost/logout"));
    }

    @Test
    public void verifyValidationWithRegEx() {
        val validator = new SimpleUrlValidatorFactoryBean(false, "\\w{2}\\.\\w{4}\\.authority", true).getObject();
        assertNotNull(validator);
        assertTrue(validator.isValid("http://my.test.authority/logout"));
        assertFalse(validator.isValid("http://mY.tEST.aUTHORITY/logout"));
        assertFalse(validator.isValid("http://other.test.authority/logout"));
        assertFalse(validator.isValid("http://localhost/logout"));
    }

    @Test
    public void verifyValidationWithRegExCaseInsensitiv() {
        val validator = new SimpleUrlValidatorFactoryBean(false, "\\w{2}\\.\\w{4}\\.authority", false).getObject();
        assertNotNull(validator);
        assertTrue(validator.isValid("http://my.test.authority/logout"));
        assertTrue(validator.isValid("http://mY.tEST.aUTHORITY/logout"));
        assertFalse(validator.isValid("http://other.test.authority/logout"));
        assertFalse(validator.isValid("http://localhost/logout"));
    }

    @Test
    public void verifyValidationWithRegExAndLocalUrlAllowed() {
        val validator = new SimpleUrlValidatorFactoryBean(true, "\\w{2}\\.\\w{4}\\.authority", true).getObject();
        assertNotNull(validator);
        assertTrue(validator.isValid("http://my.test.authority/logout"));
        assertFalse(validator.isValid("http://mY.tEST.aUTHORITY/logout"));
        assertFalse(validator.isValid("http://other.test.authority/logout"));
        assertTrue(validator.isValid("http://localhost/logout"));
    }
}
