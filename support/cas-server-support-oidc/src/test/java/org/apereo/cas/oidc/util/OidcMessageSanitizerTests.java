package org.apereo.cas.oidc.util;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.text.MessageSanitizer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcMessageSanitizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Utility")
class OidcMessageSanitizerTests extends AbstractOidcTests {

    @Autowired
    @Qualifier(MessageSanitizer.BEAN_NAME)
    private MessageSanitizer messageSanitizer;

    @Test
    void verifyOperation() {
        var results = messageSanitizer.sanitize("ticket OC-1-abcdefg created");
        assertTrue(results.contains("OC-1-********"));
        results = messageSanitizer.sanitize("ticket AT-1-abcdefg created");
        assertTrue(results.contains("AT-1-********"));
        results = messageSanitizer.sanitize("ticket RT-1-abcdefg created");
        assertTrue(results.contains("RT-1-********"));
    }
}
