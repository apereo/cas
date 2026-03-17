package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemorySamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("SAMLMetadata")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class InMemorySamlIdPMetadataLocatorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private SamlIdPMetadataDocument document;

    @BeforeEach
    void setup() throws Exception {
        document = new SamlIdPMetadataDocument(1000, "CAS",
            IOUtils.toString(new ClassPathResource("metadata/idp-metadata.xml").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-signing.crt").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-signing.key").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-encryption.crt").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-encryption.key").getInputStream(), StandardCharsets.UTF_8));
    }

    @Test
    void verifyOperation() throws Throwable {
        val locator = new InMemorySamlIdPMetadataLocator(CipherExecutor.noOpOfStringToString(),
            document, Caffeine.newBuilder().build(), applicationContext);
        locator.initialize();
        assertNotNull(locator.resolveMetadata(Optional.empty()));
        assertNotNull(locator.resolveEncryptionCertificate(Optional.empty()));
        assertNotNull(locator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(locator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(locator.resolveSigningKey(Optional.empty()));
        assertTrue(locator.exists(Optional.empty()));
    }
}
