package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.trusted.config.RestMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(RestfulApiCategory.class)
@SpringBootTest(classes = {
    RestMultifactorAuthenticationTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(properties = "cas.authn.mfa.trusted.rest.url=http://localhost:9297")
public class RestMultifactorAuthenticationTrustStorageTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private CipherExecutor mfaTrustCipherExecutor;

    @BeforeAll
    public static void setup() {
        MAPPER.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    public void verifySetAnExpireByKey() throws Exception {
        val r =
            MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap(r));
        try (val webServer = new MockWebServer(9297,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            mfaTrustEngine.set(r);
            val records = mfaTrustEngine.get("casuser");
            assertNotNull(records);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyExpireByDate() throws Exception {
        val r =
            MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        r.setRecordDate(LocalDateTime.now().minusDays(2));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap(r));
        try (val webServer = new MockWebServer(9311,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getTrusted().getRest().setUrl("http://localhost:9311");
            val mfaEngine = new RestMultifactorAuthenticationTrustStorage(new RestTemplate(), props);
            mfaEngine.setCipherExecutor(mfaTrustCipherExecutor);

            mfaEngine.set(r);
            val records = mfaEngine.get(r.getPrincipal());
            assertNotNull(records);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
