package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.model.support.mfa.GAuthMultifactorProperties;
import org.apereo.cas.util.MockWebServer;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Slf4j
public class RestGoogleAuthenticatorTokenCredentialRepositoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private IGoogleAuthenticator google;

    @Before
    public void initialize() {
        final var bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() {
        final var gauth = new GAuthMultifactorProperties();
        final var repo =
            new RestGoogleAuthenticatorTokenCredentialRepository(google, new RestTemplate(),
                gauth,
                CipherExecutor.noOpOfStringToString());
        final var acct = repo.create("casuser");
        assertNotNull(acct);
    }

    @Test
    public void verifyGet() throws Exception {
        final var gauth = new GAuthMultifactorProperties();
        gauth.getRest().setEndpointUrl("http://localhost:9295");
        final var repo =
            new RestGoogleAuthenticatorTokenCredentialRepository(google, new RestTemplate(),
                gauth,
                CipherExecutor.noOpOfStringToString());
        var acct = repo.create("casuser");

        final var data = MAPPER.writeValueAsString(acct);
        try (var webServer = new MockWebServer(9295,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
            acct = repo.get("casuser");
            assertNotNull(acct);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }


    }
}
