package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotatedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcJwksEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDCWeb")
class OidcJwksEndpointControllerTests extends AbstractOidcTests {

    private static String newKeystore(final String kid) throws Exception {
        val key = RsaJwkGenerator.generateJwk(2048);
        key.setKeyId(kid);
        return new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
    }

    private static List<JsonWebKey> fetchKeys(final OidcJwksEndpointController controller, final String kid) throws Exception {
        val response = controller.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), kid, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return new JsonWebKeySet(Objects.requireNonNull(response.getBody()).toString()).getJsonWebKeys();
    }

    @Test
    void verifyKeystoreCachedUntilModified() throws Throwable {
        val issuerService = mock(OidcIssuerService.class);
        when(issuerService.validateIssuer(any(), anyList())).thenReturn(true);
        val context = mock(OidcConfigurationContext.class);
        when(context.getIssuerService()).thenReturn(issuerService);
        when(context.getCasProperties()).thenReturn(new CasConfigurationProperties());
        val keystore = new AtomicReference<>(newKeystore("first"));
        val generator = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(generator.generate()).thenAnswer(_ -> new ByteArrayResource(keystore.get().getBytes(StandardCharsets.UTF_8)));
        val controller = new OidcJwksEndpointController(context, generator);

        assertTrue(fetchKeys(controller, "unknown").isEmpty());
        keystore.set(newKeystore("second"));
        assertEquals("first", fetchKeys(controller, null).getFirst().getKeyId());
        verify(generator, times(1)).generate();

        controller.handleOidcJsonWebKeystoreModifiedEvent(new OidcJsonWebKeystoreModifiedEvent(this,
            new File("keystore.jwks"), ClientInfo.from(new MockHttpServletRequest())));
        assertEquals("second", fetchKeys(controller, null).getFirst().getKeyId());
        verify(generator, times(2)).generate();

        keystore.set(newKeystore("third"));
        assertEquals("second", fetchKeys(controller, null).getFirst().getKeyId());
        controller.handleOidcJsonWebKeystoreRotatedEvent(new OidcJsonWebKeystoreRotatedEvent(this,
            ClientInfo.from(new MockHttpServletRequest())));
        assertEquals("third", fetchKeys(controller, null).getFirst().getKeyId());
        verify(generator, times(3)).generate();
    }

    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .queryParam("state", JsonWebKeyLifecycleStates.CURRENT.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk());
    }

    @Test
    void verifyBadEndpointRequest() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .queryParam("state", JsonWebKeyLifecycleStates.CURRENT.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor())
                .with(r -> {
                    r.setServerName("sso2.example.org");
                    return r;
                })
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyUnknownKid() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .queryParam("state", JsonWebKeyLifecycleStates.CURRENT.name().toLowerCase(Locale.ENGLISH))
                .queryParam("kid", "unknown")
                .with(withHttpRequestProcessor())
            )
            .andExpect(status().isOk());
    }

    @Test
    void verifyServiceSymmetricKeyIsNotPublished() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setJwks(EncodingUtils.generateJsonWebKey(512));
        servicesManager.save(registeredService);

        val body = mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        val jwks = JWKSet.parse(body);
        assertTrue(jwks.getKeys().stream().noneMatch(key -> KeyType.OCT.equals(key.getKeyType())));
    }
}
