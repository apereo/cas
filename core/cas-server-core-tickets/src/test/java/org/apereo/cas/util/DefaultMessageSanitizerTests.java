package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.util.text.MessageSanitizer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMessageSanitizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Utility")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultMessageSanitizerTests {

    @Autowired
    @Qualifier(MessageSanitizer.BEAN_NAME)
    private MessageSanitizer messageSanitizer;

    @Test
    void verifyOperation() {
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
        assertTrue(results.matches(".+,clientSecret = \\*\\*\\*\\*\\*\\*\\*\\*.*\\.\\.\\."));

        results = messageSanitizer.sanitize("'password' -> array<String>['Mellon']");
        assertTrue(results.matches("'password' -\\> array\\<String\\>\\['.*'\\]"));
    }
}
