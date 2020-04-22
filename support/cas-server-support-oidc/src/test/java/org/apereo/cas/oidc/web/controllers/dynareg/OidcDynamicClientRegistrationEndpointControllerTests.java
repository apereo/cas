package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcDynamicClientRegistrationEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("oidcDynamicClientRegistrationEndpointController")
    protected OidcDynamicClientRegistrationEndpointController controller;

    @Test
    public void verifyBadInput() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertEquals(HttpStatus.SC_BAD_REQUEST, controller.handleRequestInternal("bad-input", request, response).getStatusCodeValue());
    }

    @Test
    public void verifyOperation() throws Exception {
        val registrationReq = '{'
            + "   \"application_type\": \"web\","
            + "   \"default_acr_values\":"
            + "     [\"mfa-duo\",\"mfa-gauth\"],"
            + "   \"redirect_uris\":"
            + "     [\"https://client.example.org/callback\","
            + "      \"https://client.example.org/callback2\"],"
            + "   \"client_name\": \"My Example\","
            + "   \"client_name#ja-Jpan-JP\":"
            + "     \"クライアント名\","
            + "   \"logo_uri\": \"https://client.example.org/logo.png\","
            + "   \"subject_type\": \"pairwise\","
            + "   \"sector_identifier_uri\":"
            + "     \"http://localhost:7711\","
            + "   \"token_endpoint_auth_method\": \"client_secret_basic\","
            + "   \"jwks_uri\": \"https://client.example.org/my_public_keys.jwks\","
            + "   \"userinfo_encrypted_response_alg\": \"RSA1_5\","
            + "   \"userinfo_encrypted_response_enc\": \"A128CBC-HS256\","
            + "   \"contacts\": [\"ve7jtb@example.org\", \"mary@example.org\"],"
            + "   \"request_uris\":"
            + "     [\"https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA\"]"
            + "  }";

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val entity = MAPPER.writeValueAsString(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
        try (val webServer = new MockWebServer(7711,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), org.springframework.http.HttpStatus.OK)) {
            webServer.start();
            assertEquals(HttpStatus.SC_CREATED, controller.handleRequestInternal(registrationReq, request, response).getStatusCodeValue());
        }
    }
}
