package org.apereo.cas.web;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author swoeste
 * @since 5.1.0
 */
@RunWith(JUnit4.class)
public class SimpleUrlValidatorFactoryBeanTests {

    @Test
    public void verifyValidation() throws Exception {
        final UrlValidator validator = new SimpleUrlValidatorFactoryBean(false).getObject();
        assertTrue(validator.isValid("http://www.demo.com/logout"));
        assertFalse(validator.isValid("http://localhost/logout"));
    }

    @Test
    public void verifyValidationWithLocalUrlAllowed() throws Exception {
        final UrlValidator validator = new SimpleUrlValidatorFactoryBean(true).getObject();
        assertTrue(validator.isValid("http://www.demo.com/logout"));
        assertTrue(validator.isValid("http://localhost/logout"));
    }

}
