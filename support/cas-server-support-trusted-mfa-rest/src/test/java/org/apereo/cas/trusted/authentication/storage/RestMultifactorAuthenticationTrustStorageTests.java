package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.config.RestMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.keys.DefaultMultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
@SpringBootTest(classes = {
    RestMultifactorAuthenticationTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    WebMvcAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw",

        "cas.authn.mfa.trusted.crypto.encryption.key=zAaKugaeAUSEfS8MCAdQbj4rxgHRLpNvgjLs4Mr6iiM",
        "cas.authn.mfa.trusted.crypto.signing.key=dU33-XjGeq8WhaAWCs1r1pPvgiLh_rQTgfANUq4hZcktvvhwOe6RXaeddMc446afK3emoOO4ZQpX85IBfAAQYA",
        
        "cas.authn.mfa.trusted.rest.url=http://localhost:9297"
    })
class RestMultifactorAuthenticationTrustStorageTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private CipherExecutor<Serializable, String> mfaTrustCipherExecutor;

    @BeforeAll
    public static void setup() {
        MAPPER.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    void verifyRemovalByKey() throws Throwable {
        val r = MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap(r));
        try (val webServer = new MockWebServer(9297,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertDoesNotThrow(() -> mfaTrustEngine.remove(r.getRecordKey()));
        }
    }

    @Test
    void verifyRemovalByDate() throws Throwable {
        try (val webServer = new MockWebServer(9297,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertDoesNotThrow(() -> mfaTrustEngine.remove(ZonedDateTime.now(ZoneOffset.UTC)));
        }
    }

    @Test
    void verifyFetchRecords() throws Throwable {
        val r = MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap(r));
        try (val webServer = new MockWebServer(9297,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            mfaTrustEngine.save(r);
            val record = mfaTrustEngine.get(r.getId());
            assertNotNull(record);
            assertNotNull(mfaTrustEngine.getAll());
        }
    }

    @Test
    void verifySetAnExpireByKey() throws Throwable {
        val r =
            MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap(r));
        try (val webServer = new MockWebServer(9297,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            mfaTrustEngine.save(r);
            val records = mfaTrustEngine.get("casuser");
            assertNotNull(records);
        }
    }

    @Test
    void verifyExpireByDate() throws Throwable {
        val r = MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        r.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).minusDays(2));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap(r));
        try (val webServer = new MockWebServer(9311,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getAuthn().getMfa().getTrusted().getRest().setUrl("http://localhost:9311");
            val mfaEngine = new RestMultifactorAuthenticationTrustStorage(props.getAuthn().getMfa().getTrusted(),
                mfaTrustCipherExecutor, new DefaultMultifactorAuthenticationTrustRecordKeyGenerator(),
                new RestTemplate());
            mfaEngine.save(r);
            val records = mfaEngine.get(r.getPrincipal());
            assertNotNull(records);
        }
    }
}
