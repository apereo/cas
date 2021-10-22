package org.apereo.cas.util.serialization;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ToStringBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MessageSanitizationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Utility")
public class MessageSanitizationUtilsTests {

    @Test
    public void verifyOperation() {
        var results = MessageSanitizationUtils.sanitize("ticket TGT-1-abcdefg created");
        assertTrue(results.contains("TGT-1-*****"));
        results = MessageSanitizationUtils.sanitize("ticket PGT-1-abcdefg created");
        assertTrue(results.contains("PGT-1-*****"));
        results = MessageSanitizationUtils.sanitize("ticket PGTIOU-1-abcdefg created");
        assertTrue(results.contains("PGTIOU-1-*****"));
        results = MessageSanitizationUtils.sanitize("ticket OC-1-abcdefg created");
        assertTrue(results.contains("OC-1-*****"));
        results = MessageSanitizationUtils.sanitize("ticket AT-1-abcdefg created");
        assertTrue(results.contains("AT-1-*****"));

        results = MessageSanitizationUtils.sanitize("found a [password =se!ns4357$##@@**it!!_ive] here...");
        assertTrue(results.contains("[password =*****"));

        results = MessageSanitizationUtils.sanitize(new ToStringBuilder(this)
            .append("password", "abcdefgs")
            .append("field", "value")
            .toString());
        assertTrue(results.contains("password = *****"));

        results = MessageSanitizationUtils.sanitize("found a [token=mgf63isnfb1s!!#ut0__|] here...");
        assertTrue(results.contains("[token=*****"));
    }
}
