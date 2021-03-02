package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.BaseAccepttoMultifactorAuthenticationTests;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoWebflowUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccepttoQRCodeValidateWebSocketChannelActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseAccepttoMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.acceptto.api-url=http://localhost:5012",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.secret=thisisasecret",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
    })
@Tag("WebflowMfaActions")
public class AccepttoQRCodeValidateWebSocketChannelActionTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaAccepttoDistributedSessionStore")
    private SessionStore mfaAccepttoDistributedSessionStore;

    @Test
    public void verifyOperation() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        httpRequest.addParameter("channel", "test-channel");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("success", "true", "user_email", "cas@example.org"));
        try (val webServer = new MockWebServer(5012,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val action = new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), httpRequest, response));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
            RequestContextHolder.setRequestContext(context);
            AccepttoWebflowUtils.setChannel(context, "test-channel");

            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, result.getId());
            assertTrue(WebUtils.getCredential(context) instanceof AccepttoEmailCredential);
        }
    }

    @Test
    public void verifyNoSuccess() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        httpRequest.addParameter("channel", "test-channel");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("success", "false", "message", "error"));
        try (val webServer = new MockWebServer(5012,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val action = new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), httpRequest, response));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
            RequestContextHolder.setRequestContext(context);
            AccepttoWebflowUtils.setChannel(context, "test-channel");

            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
        }
    }

    @Test
    public void verifyForbidden() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        httpRequest.addParameter("channel", "test-channel");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("success", "false", "user_email", "cas@example.org"));
        try (val webServer = new MockWebServer(5012,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.FORBIDDEN)) {
            webServer.start();
            val action = new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), httpRequest, response));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
            RequestContextHolder.setRequestContext(context);
            AccepttoWebflowUtils.setChannel(context, "test-channel");

            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
        }
    }

    @Test
    public void verifyUnauthz() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        httpRequest.addParameter("channel", "test-channel");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("success", "false", "user_email", "cas@example.org"));
        try (val webServer = new MockWebServer(5012,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.UNAUTHORIZED)) {
            webServer.start();
            val action = new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), httpRequest, response));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
            RequestContextHolder.setRequestContext(context);
            AccepttoWebflowUtils.setChannel(context, "test-channel");

            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
        }
    }

    @Test
    public void verifyBadPayload() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        httpRequest.addParameter("channel", "test-channel");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));

        try (val webServer = new MockWebServer(5012,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val action = new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), httpRequest, response));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
            RequestContextHolder.setRequestContext(context);
            AccepttoWebflowUtils.setChannel(context, "test-channel");

            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
        }
    }

    
    @Test
    public void verifyMissingChannel() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("success", "true", "user_email", "cas@example.org"));
        try (val webServer = new MockWebServer(5012,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val action = new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), httpRequest, response));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
            RequestContextHolder.setRequestContext(context);
            AccepttoWebflowUtils.setChannel(context, "test-channel");

            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
        }
    }
}
