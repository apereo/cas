package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.config.CasRestMultifactorAuthenticationTrustAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasRestMultifactorAuthenticationTrustAutoConfiguration.class,
    CasMultifactorAuthnTrustAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
        "cas.authn.mfa.trusted.device-fingerprint.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw",

        "cas.authn.mfa.trusted.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.mfa.trusted.crypto.encryption.key=zAaKugaeAUSEfS8MCAdQbj4rxgHRLpNvgjLs4Mr6iiM",
        "cas.authn.mfa.trusted.crypto.signing.key=dU33-XjGeq8WhaAWCs1r1pPvgiLh_rQTgfANUq4hZcktvvhwOe6RXaeddMc446afK3emoOO4ZQpX85IBfAAQYA",

        "cas.authn.mfa.trusted.rest.url=http://localhost:${random.int[3000,9000]}"
    })
class RestMultifactorAuthenticationTrustStorageTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).writeDatesAsTimestamps(false).build().toObjectMapper();

    @Autowired
    @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyRemovalByDateAndHttpErrors() {
        val builder = RestClient.builder();
        val server = MockRestServiceServer.bindTo(builder).build();
        val properties = new TrustedDevicesMultifactorProperties();
        properties.getRest().setUrl("https://localhost/trusted");
        properties.getRest().setBasicAuthUsername("username");
        properties.getRest().setBasicAuthPassword("password");
        val storage = new RestMultifactorAuthenticationTrustStorage(properties, CipherExecutor.noOpOfSerializableToString(),
            mock(MultifactorAuthenticationTrustRecordKeyGenerator.class), builder.build());
        val expiration = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        server.expect(requestTo("https://localhost/trusted/"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic "
                + HttpHeaders.encodeBasicAuth("username", "password", StandardCharsets.UTF_8)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(MAPPER.writeValueAsString(expiration)))
            .andRespond(withSuccess());
        server.expect(requestTo("https://localhost/trusted/"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        storage.remove(expiration);
        val exception = assertThrows(RestClientResponseException.class, storage::getAll);
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        server.verify();
    }

    @Test
    void verifyRemovalByKey() {
        val port = URI.create(casProperties.getAuthn().getMfa().getTrusted().getRest().getUrl()).getPort();

        try (val webServer = new MockWebServer(port)) {
            webServer.start();
            webServer.setContentType(MediaType.APPLICATION_JSON_VALUE);

            val record = MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
            webServer.responseBody(MAPPER.writeValueAsString(CollectionUtils.wrap(record)));

            assertDoesNotThrow(() -> mfaTrustEngine.remove(record.getRecordKey()));

            webServer.responseBody(MAPPER.writeValueAsString(CollectionUtils.wrap(record)));
            mfaTrustEngine.save(record);
            val saved = mfaTrustEngine.get(record.getId());
            assertNotNull(saved);
            assertNotNull(mfaTrustEngine.getAll());
            val records = mfaTrustEngine.get("casuser");
            assertNotNull(records);
            
            record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).minusDays(2));
            webServer.responseBody(MAPPER.writeValueAsString(CollectionUtils.wrap(record)));
            mfaTrustEngine.save(record);
            assertNotNull(mfaTrustEngine.get(record.getPrincipal()));
        }
    }
}
