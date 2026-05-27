package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.introspection.BaseOAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenFailureResponse;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.HttpConstants;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20IntrospectionEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuthWeb")
class OAuth20IntrospectionEndpointControllerTests extends AbstractOAuth20Tests {

    private static final String CLIENT_ID2 = "2";

    @Test
    void verifyBadCredentialsOperation() throws Throwable {
        val registeredService = addRegisteredService();
        val body = (OAuth20IntrospectionAccessTokenFailureResponse) internalVerifyOperation("---", registeredService);
        assertNotNull(body.getError());
    }

    @Test
    void verifyOperation() throws Throwable {
        val service = addRegisteredService();
        val auth = service.getClientId() + ':' + CLIENT_SECRET;
        val body = (OAuth20IntrospectionAccessTokenResponse) internalVerifyOperation(auth, service);

        assertNotNull(body);
        assertEquals(service.getClientId(), body.getClientId());
        assertEquals(SERVICE_URL, body.getAud());
    }

    @Test
    void verifyBadSecret() throws Throwable {
        val service = addRegisteredService(SERVICE_URL, UUID.randomUUID().toString());
        val auth = service.getClientId() + ':' + CLIENT_SECRET;
        val body = (OAuth20IntrospectionAccessTokenFailureResponse) internalVerifyOperation(auth, service);
        assertNotNull(body.getError());
    }

    @Test
    void verifyOperationFromOtherClient() throws Throwable {
        val registeredService2 = getRegisteredService(REDIRECT_URI, CLIENT_ID2, CLIENT_SECRET);
        servicesManager.save(registeredService2);
        val registeredService = addRegisteredService();

        val auth2 = CLIENT_ID2 + ':' + CLIENT_SECRET;
        val body = (OAuth20IntrospectionAccessTokenResponse) internalVerifyOperation(auth2, registeredService);
        assertNotNull(body);
        assertEquals(registeredService.getClientId(), body.getClientId());
        assertEquals(SERVICE_URL, body.getAud());
    }

    @Test
    void verifyNoService() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.INTROSPECTION_URL);

        val id = UUID.randomUUID().toString();
        val auth = id + ':' + CLIENT_SECRET;
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val registeredService = getRegisteredService(id, id);
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        request.addParameter(OAuth20Constants.ACCESS_TOKEN, at);
        val result = performOAuthRequest(request);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyUnauthzOperation() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.INTROSPECTION_URL);
        val result = performOAuthRequest(request);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyBadOperation() throws Throwable {
        val service = addRegisteredService();
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.INTROSPECTION_URL);
        val auth = service.getClientId() + ':' + CLIENT_SECRET;
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);
        val result = performOAuthRequest(request);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    protected BaseOAuth20IntrospectionAccessTokenResponse internalVerifyOperation(final String auth,
                                                                                  final OAuthRegisteredService registeredService) throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.INTROSPECTION_URL);

        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        request.addParameter(OAuth20Constants.TOKEN, at);
        val result = performOAuthRequest(request);
        assertTrue(result.getResponse().getStatus() == HttpStatus.OK.value()
            || result.getResponse().getStatus() == HttpStatus.UNAUTHORIZED.value()
            || result.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value());
        val payload = result.getResponse().getContentAsString();
        if (result.getResponse().getStatus() == HttpStatus.OK.value()) {
            return MAPPER.readValue(payload, OAuth20IntrospectionAccessTokenResponse.class);
        }
        return MAPPER.readValue(payload, OAuth20IntrospectionAccessTokenFailureResponse.class);
    }
}
