package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20IntrospectionEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20IntrospectionEndpointControllerTests extends AbstractOAuth20Tests {

    private static final String CLIENT_ID2 = "2";

    @Autowired
    @Qualifier("introspectionEndpointController")
    private OAuth20IntrospectionEndpointController<OAuth20ConfigurationContext> introspectionEndpoint;

    @Test
    public void verifyBadCredentialsOperation() {
        val body = internalVerifyOperation("---");
        assertNull(body);
    }

    @Test
    public void verifyOperation() {
        val auth = CLIENT_ID + ':' + CLIENT_SECRET;
        addRegisteredService();
        val body = internalVerifyOperation(auth);

        assertNotNull(body);
        assertEquals(CLIENT_ID, body.getClientId());
        assertEquals(SERVICE_URL, body.getAud());
    }

    @Test
    public void verifyOperationFromOtherClient() {
        val registeredService2 = getRegisteredService(REDIRECT_URI, CLIENT_ID2, CLIENT_SECRET);
        servicesManager.save(registeredService2);
        
        val auth2 = CLIENT_ID2 + ':' + CLIENT_SECRET;
        val body = internalVerifyOperation(auth2);

        assertNotNull(body);
        assertEquals(CLIENT_ID, body.getClientId());
        assertEquals(SERVICE_URL, body.getAud());
    }

    @Test
    public void verifyNoService() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val id = UUID.randomUUID().toString();
        val auth = id + ':' + CLIENT_SECRET;
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val registeredService = getRegisteredService(id, id);
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        request.addParameter(OAuth20Constants.ACCESS_TOKEN, at);
        val result = introspectionEndpoint.handleRequest(request, response);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    public void verifyUnauthzOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val result = introspectionEndpoint.handleRequest(request, response);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    public void verifyBadOperation() {
        addRegisteredService();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val auth = CLIENT_ID + ':' + CLIENT_SECRET;
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);
        val result = introspectionEndpoint.handleRequest(request, response);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    protected OAuth20IntrospectionAccessTokenResponse internalVerifyOperation(final String auth) {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val registeredService = addRegisteredService();
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        request.addParameter(OAuth20Constants.TOKEN, at);
        return introspectionEndpoint.handleRequest(request, response).getBody();
    }
}
