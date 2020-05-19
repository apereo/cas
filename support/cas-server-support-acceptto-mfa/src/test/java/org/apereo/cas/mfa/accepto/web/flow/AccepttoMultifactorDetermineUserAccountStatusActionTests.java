package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.BaseAccepttoMultifactorAuthenticationTests;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccepttoMultifactorDetermineUserAccountStatusActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
@SpringBootTest(classes = BaseAccepttoMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.acceptto.registrationApiUrl=http://localhost:5013",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.group-attribute=group",
        "cas.authn.mfa.acceptto.email-attribute=email",
        "cas.authn.mfa.acceptto.secret=255724611137f7eb0280dd76b0546eea4bca1c7ba1",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=255724611137f7eb0280dd76b0546eea4bca1c7ba1",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AccepttoMultifactorDetermineUserAccountStatusActionTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperationRegister() throws Exception {
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

        val inviteToken = MAPPER.writeValueAsString(Map.of("invitation_token", UUID.randomUUID().toString()));
        val payload = MAPPER.writeValueAsString(Map.of("invite_token", EncodingUtils.encodeBase64(inviteToken),
            "success", "true",
            "eguardian_user_id", "cas-user",
            "channel", UUID.randomUUID().toString(),
            "response_code", "pair_device"));
        val jwt = EncodingUtils.signJwsRSASha512(priv, payload.getBytes(StandardCharsets.UTF_8), Map.of());

        val data = MAPPER.writeValueAsString(Map.of("content", new String(jwt, StandardCharsets.UTF_8)));
        try (val webServer = new MockWebServer(5013,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val action = new AccepttoMultifactorDetermineUserAccountStatusAction(casProperties, pub);
            WebUtils.putAuthentication(authentication, context);
            RequestContextHolder.setRequestContext(context);

            val result = action.doExecute(context);
            assertEquals(result.getId(), CasWebflowConstants.TRANSITION_ID_REGISTER);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }

    }
}
