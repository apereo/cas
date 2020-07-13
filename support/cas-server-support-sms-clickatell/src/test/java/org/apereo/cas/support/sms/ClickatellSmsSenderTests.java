package org.apereo.cas.support.sms;

import org.apereo.cas.config.ClickatellSmsConfiguration;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is {@link ClickatellSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    ClickatellSmsConfiguration.class
}, properties = {
    "cas.sms-provider.clickatell.server-url=http://localhost:8099",
    "cas.sms-provider.clickatell.token=DEMO_TOKEN"
})
@Tag("SMS")
public class ClickatellSmsSenderTests {
    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Test
    public void verifySmsSender() {
        val data = "{\n"
            + "\"messages\": [\n"
            + "{\n"
            + "\"apiMessageId\": \"77fb29998253415fa5d66971d519d362\",\n"
            + "\"accepted\": true,\n"
            + "\"to\": \"380976543211\",\n"
            + "\"error\": null\n"
            + "},\n"
            + "{\n"
            + "\"apiMessageId\": \"d2a7b3f2a72a4c798f3f385ee92ee5ce\",\n"
            + "\"accepted\": true,\n"
            + "\"to\": \"380976543212\",\n"
            + "\"error\": null\n"
            + "}\n"
            + "],\n"
            + "\"error\": null\n"
            + '}';

        try (val webServer = new MockWebServer(8099,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }

    @Test
    public void verifyError() {
        val data = "{\n"
            + "\"messages\": [\n"
            + "],\n"
            + "\"error\": \"error message\"\n"
            + '}';

        try (val webServer = new MockWebServer(8099,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }

    @Test
    public void verifyBadSmsSender() {
        try (val webServer = new MockWebServer(8099,
            new ByteArrayResource("{}".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }
}
