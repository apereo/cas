package org.apereo.cas.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegexUtilsTest {

    @Test
    public void verifyNotValidRegex() throws Exception {
        String notValidRegex = "$$$$";

        assertFalse(RegexUtils.isValidRegex(notValidRegex));
    }
}