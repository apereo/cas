package org.apereo.cas.support.sms;

import org.apereo.cas.config.CasSmsModeSmsAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import java.net.URI;
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
    WebMvcAutoConfiguration.class,
    CasSmsModeSmsAutoConfiguration.class
},
    properties = "cas.sms-provider.sms-mode.url=http://localhost:${random.int[4000,9999]}")
@Tag("SMS")
class SmsModeSmsSenderTests {
    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Autowired
    private CasConfigurationProperties casProperties;

    private int getCurrentPort() throws Throwable {
        val url = casProperties.getSmsProvider().getSmsMode().getUrl();
        return new URI(url).getPort();
    }

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(smsSender);
        assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        try (val webServer = new MockWebServer(getCurrentPort(),
                new ByteArrayResource("0".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        }
    }
}
