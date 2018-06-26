package org.apereo.cas.support.sms;

import org.apereo.cas.config.ClickatellSmsConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.io.SmsSender;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link ClickatellSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    ClickatellSmsConfiguration.class
})
@RunWith(ConditionalSpringRunner.class)
@TestPropertySource(locations = "classpath:clickatell.properties")
public class ClickatellSmsSenderTests {
    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    private MockWebServer webServer;

    @Before
    public void initialize() {
        final var data = "{\n"
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
        this.webServer = new MockWebServer(8099,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }

    @After
    public void cleanup() {
        this.webServer.stop();
    }

    @Test
    public void verifySmsSender() {
        assertTrue(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
    }
}
