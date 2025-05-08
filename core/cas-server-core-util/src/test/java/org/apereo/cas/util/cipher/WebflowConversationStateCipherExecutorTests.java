package org.apereo.cas.util.cipher;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebflowConversationStateCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Cipher")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class WebflowConversationStateCipherExecutorTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyAction() {
        val cipher = new WebflowConversationStateCipherExecutor("AES", 512, 16);
        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    void verifyCipherWithProps() {
        val crypto = casProperties.getWebflow().getCrypto();
        val cipher = new WebflowConversationStateCipherExecutor(
            "P4fxK62MCY5xL5y1DGb3_Q",
            "mpO02yZuW-QowasD_Eo64WsH4Tg75vPqV4KQaI2B5BMiQ-cFm3vHC7lJGJOYToGK6l7Bi_0_jmnZrg8wh1iPZA",
            crypto.getAlg(),
            crypto.getSigning().getKeySize(),
            crypto.getEncryption().getKeySize());

        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    void verifyCipherWithoutSigning() {
        val crypto = casProperties.getWebflow().getCrypto();
        val cipher = new WebflowConversationStateCipherExecutor(
            "P4fxK62MCY5xL5y1DGb3_Q",
            "mpO02yZuW-QowasD_Eo64WsH4Tg75vPqV4KQaI2B5BMiQ-cFm3vHC7lJGJOYToGK6l7Bi_0_jmnZrg8wh1iPZA",
            crypto.getAlg(),
            crypto.getSigning().getKeySize(),
            crypto.getEncryption().getKeySize());

        val withoutSigning = cipher.withSigningDisabled();
        val encoded = withoutSigning.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(withoutSigning.decode(encoded), StandardCharsets.UTF_8));
    }
}
