package org.apereo.cas.support.sms;

import org.apereo.cas.config.SmsModeSmsConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

/**
 * This is {@link SmsModeSmsSenderTests}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    SmsModeSmsConfiguration.class
},
    properties = "cas.sms-provider.sms-mode.url=http://localhost:8099")
@Tag("SMS")
public class SmsModeSmsSenderTests {
    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Test
    public void verifyOperation() {
        assertNotNull(smsSender);
        assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        try (val webServer = new MockWebServer(8099,
            new ByteArrayResource("0".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }
}
