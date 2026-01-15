package org.apereo.cas.support.sms;

import module java.base;
import org.apereo.cas.config.CasClickatellSmsAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasClickatellSmsAutoConfiguration.class, properties = {
    "cas.sms-provider.clickatell.server-url=http://localhost:${random.int[3000,9000]}",
    "cas.sms-provider.clickatell.token=DEMO_TOKEN"
})
@Tag("SMS")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class ClickatellSmsSenderTests {
    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifySmsSender() throws Throwable {
        val data = '{'
            + "\"messages\": ["
            + '{'
            + "\"apiMessageId\": \"77fb29998253415fa5d66971d519d362\","
            + "\"accepted\": true,"
            + "\"to\": \"380976543211\","
            + "\"error\": null"
            + "},"
            + '{'
            + "\"apiMessageId\": \"d2a7b3f2a72a4c798f3f385ee92ee5ce\","
            + "\"accepted\": true,"
            + "\"to\": \"380976543212\","
            + "\"error\": null"
            + '}'
            + "],"
            + "\"error\": null"
            + '}';

        val props = casProperties.getSmsProvider().getClickatell();
        val port = URI.create(props.getServerUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }

    @Test
    void verifyError() throws Throwable {
        val data = '{'
            + "\"messages\": ["
            + "],"
            + "\"error\": \"error message\","
            + "\"accepted\": \"false\""
            + '}';

        val props = casProperties.getSmsProvider().getClickatell();
        val port = URI.create(props.getServerUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }

    @Test
    void verifyUnacceptable() throws Throwable {
        val data = '{'
            + "\"messages\": ["
            + "{\"accepted\": \"false\", \"error\": \"fails\"}"
            + ']'
            + '}';

        val props = casProperties.getSmsProvider().getClickatell();
        val port = URI.create(props.getServerUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }

    @Test
    void verifyBadPayload() throws Throwable {
        val data = '{'
            + "\"messages\": ["
            + "{\"accepted\":..."
            + ']'
            + '}';

        val props = casProperties.getSmsProvider().getClickatell();
        val port = URI.create(props.getServerUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }

    @Test
    void verifyBadSmsSender() throws Throwable {
        val props = casProperties.getSmsProvider().getClickatell();
        val port = URI.create(props.getServerUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource("{}".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }
}
