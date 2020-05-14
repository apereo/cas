package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.mfa.AccepttoMultifactorProperties;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoWebflowUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccepttoApiUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFA")
public class AccepttoApiUtilsTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyEmail() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            CoreAuthenticationTestUtils.getPrincipal(Map.of("email", List.of("cas@example.org"))));

        val properties = new AccepttoMultifactorProperties();
        properties.setEmailAttribute("email");
        assertNotNull(AccepttoApiUtils.getUserEmail(authentication, properties));
    }

    @Test
    public void verifyGroup() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            CoreAuthenticationTestUtils.getPrincipal(Map.of("group", List.of("staff"))));

        val properties = new AccepttoMultifactorProperties();
        properties.setGroupAttribute("group");
        assertNotNull(AccepttoApiUtils.getUserGroup(authentication, properties));
    }

    @Test
    public void verifyUserValid() throws Exception {
        val properties = new AccepttoMultifactorProperties();
        properties.setGroupAttribute("group");
        properties.setEmailAttribute("email");
        properties.setApplicationId("appid");
        properties.setSecret("p@$$w0rd");
        properties.setApiUrl("http://localhost:9289");
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of(
            "email", List.of("cas@example.org"),
            "group", List.of("staff")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);

        val data = MAPPER.writeValueAsString(Map.of("device_paired", "true"));
        try (val webServer = new MockWebServer(9289,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val user = AccepttoApiUtils.isUserValid(authentication, properties);
            assertFalse(user.isEmpty());

        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyUserDevicePaired() throws Exception {
        val properties = new AccepttoMultifactorProperties();
        properties.setGroupAttribute("group");
        properties.setEmailAttribute("email");
        properties.setApplicationId("appid");
        properties.setSecret("p@$$w0rd");
        properties.setApiUrl("http://localhost:9288");
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of(
            "email", List.of("cas@example.org"),
            "group", List.of("staff")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);

        val data = MAPPER.writeValueAsString(Map.of("device_paired", "true"));
        try (val webServer = new MockWebServer(9288,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(AccepttoApiUtils.isUserDevicePaired(authentication, properties));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyQR() throws Exception {
        val properties = new AccepttoMultifactorProperties();
        properties.setGroupAttribute("group");
        properties.setEmailAttribute("email");
        properties.setApplicationId("appid");
        properties.setSecret("p@$$w0rd");
        properties.setApiUrl("http://localhost:9289");
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of(
            "email", List.of("cas@example.org"),
            "group", List.of("staff")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val hash = AccepttoApiUtils.generateQRCodeHash(authentication, properties, UUID.randomUUID().toString());
        assertNotNull(hash);
        assertNotNull(AccepttoApiUtils.decodeInvitationToken(hash));
    }

    @Test
    public void verifyAuthenticate() throws Exception {
        val properties = new AccepttoMultifactorProperties();
        properties.setGroupAttribute("group");
        properties.setEmailAttribute("email");
        properties.setApplicationId("appid");
        properties.setSecret("p@$$w0rd");
        properties.setRegistrationApiUrl("http://localhost:9285");
        properties.setOrganizationId("org-id");
        properties.setOrganizationSecret("255724611137f7eb0280dd76b0546eea4bca1c7ba1");

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of(
            "email", List.of("cas@example.org"),
            "group", List.of("staff")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setCookies(new Cookie("jwt", UUID.randomUUID().toString()));
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        AccepttoWebflowUtils.setEGuardianUserId(context, "eguardian-userid");
        WebUtils.putCredential(context, new AccepttoEmailCredential("cas@example.org"));

        val keyGen = KeyPairGenerator.getInstance("RSA");
        val pair = keyGen.generateKeyPair();
        val priv = pair.getPrivate();
        val pub = pair.getPublic();
        val payload = MAPPER.writeValueAsString(Map.of("uid", "casuser"));
        val jwt = EncodingUtils.signJwsRSASha512(priv, payload.getBytes(StandardCharsets.UTF_8), Map.of());

        val data = MAPPER.writeValueAsString(Map.of("content", new String(jwt, StandardCharsets.UTF_8)));
        try (val webServer = new MockWebServer(9285,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val results = AccepttoApiUtils.authenticate(authentication, properties, context, pub);
            assertNotNull(results);

        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
