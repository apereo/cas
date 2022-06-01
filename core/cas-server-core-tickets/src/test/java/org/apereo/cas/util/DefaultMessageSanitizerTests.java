package org.apereo.cas.util;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.text.MessageSanitizer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMessageSanitizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Utility")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreUtilConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DefaultMessageSanitizerTests {

    @Autowired
    @Qualifier(MessageSanitizer.BEAN_NAME)
    private MessageSanitizer messageSanitizer;

    @Test
    public void verifyOperation() {
        var results = messageSanitizer.sanitize("ticket TGT-1-abcdefg created");
        assertTrue(results.contains("TGT-1-********"));
        results = messageSanitizer.sanitize("ticket PGT-1-abcdefg created");
        assertTrue(results.contains("PGT-1-********"));
        results = messageSanitizer.sanitize("ticket PGTIOU-1-abcdefg created");
        assertTrue(results.contains("PGTIOU-1-********"));
        results = messageSanitizer.sanitize("ticket PT-1-abcdefg created");
        assertTrue(results.contains("PT-1-********"));
        results = messageSanitizer.sanitize("ticket ST-1-abcdefg created");
        assertTrue(results.contains("ST-1-********"));

        results = messageSanitizer.sanitize("found a [password =se!ns4357$##@@**it!!_ive] here...");
        assertTrue(results.contains("[password =********"));

        results = messageSanitizer.sanitize(new ToStringBuilder(this)
            .append("password", "abcdefgs")
            .append("field", "value")
            .toString());
        assertTrue(results.contains("password = ********"));

        results = messageSanitizer.sanitize("found a [token=mgf63isnfb1s!!#ut0__|] here...");
        assertTrue(results.contains("[token=********"));

        results = messageSanitizer.sanitize("found a ,clientSecret = p@$$wordSecret...");
        assertTrue(results.contains(",clientSecret = ********..."));
    }
}
