package org.apereo.cas.support.sms;

import module java.base;
import org.apereo.cas.config.CasSmsModeSmsAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is {@link SmsModeSmsSenderTests}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasSmsModeSmsAutoConfiguration.class, properties = "cas.sms-provider.sms-mode.url=http://localhost:${random.int[4000,9999]}")
@Tag("SMS")
@ExtendWith(CasTestExtension.class)
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
