package org.apereo.cas.support.oauth.validator.authorization;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OAuth")
class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests extends AbstractOAuth20Tests {
    private OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator getValidator(
        final ServicesManager serviceManager) {
        return new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(serviceManager,
            serviceFactory,
            new RegisteredServiceAccessStrategyAuditableEnforcer(applicationContext, principalAccessStrategyEnforcer),
            oauthRequestParameterResolver);
    }

    @Test
    void verifyUnsignedRequestParameter() throws Throwable {
        val clientId = UUID.randomUUID().toString();
        addRegisteredService(Set.of(), clientId, UUID.randomUUID().toString(), "https://.+");
        val validator = getValidator(servicesManager);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val claims = """
            {"scope":"openid","response_type":"code","redirect_uri":"https://staging.certification.openid.net/test/a/CAS/callback",\
            "state":"vIN1b0Y4Ck","nonce":"1N9mqPO9ft","client_id":"%s"}""".formatted(clientId);
        val authnRequest = "eyJhbGciOiJub25lIn0."
            + Base64.getUrlEncoder().withoutPadding().encodeToString(claims.getBytes(StandardCharsets.UTF_8)) + '.';

        request.setParameter(OAuth20Constants.REQUEST, authnRequest);
        assertTrue(validator.supports(request, response));
        assertTrue(validator.validate(request, response));
    }

    @Test
    void verifyRequestedResources() throws Throwable {
        val service = addRegisteredService("https://.+", UUID.randomUUID().toString());
        val allowedResource = "https://api.example.org/";
        service.setAllowedResources(Set.of(allowedResource));
        val validator = getValidator(servicesManager);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        request.addParameter(OAuth20Constants.RESOURCE, allowedResource);
        val response = new MockHttpServletResponse();

        assertTrue(validator.supports(request, response));

        request.setParameter(OAuth20Constants.RESOURCE, "https://other.example.org/");
        assertFalse(validator.supports(request, response));
        assertEquals(OAuth20Constants.INVALID_TARGET, request.getAttribute(OAuth20Constants.ERROR).toString());

        request.setParameter(OAuth20Constants.RESOURCE, "https://api.example.org/#fragment");
        assertFalse(validator.supports(request, response));
        assertEquals(OAuth20Constants.INVALID_TARGET, request.getAttribute(OAuth20Constants.ERROR).toString());

        service.setAllowedResources(Set.of("https://api\\.example\\.org/(orders|reports)/?"));
        servicesManager.save(service);
        request.setParameter(OAuth20Constants.RESOURCE, "https://api.example.org/orders");
        assertTrue(validator.supports(request, response));
        request.setParameter(OAuth20Constants.RESOURCE, "https://api.example.org/orders/../admin");
        assertFalse(validator.supports(request, response));

        request.setParameter(OAuth20Constants.RESOURCE, "https://cas.example.org/cas/mcp");
        assertTrue(validator.supports(request, response));
        service.setAllowedResources(Set.of());
        servicesManager.save(service);
        assertTrue(validator.supports(request, response));
        request.setParameter(OAuth20Constants.RESOURCE, "https://cas.example.org/cas/mcp/other");
        assertFalse(validator.supports(request, response));
        assertEquals(OAuth20Constants.INVALID_TARGET, request.getAttribute(OAuth20Constants.ERROR).toString());
    }

    @Test
    void verifyValidator() throws Throwable {
        val service = addRegisteredService("https://.+", UUID.randomUUID().toString());
        val validator = getValidator(servicesManager);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));
        assertEquals("Missing required parameter: [" + OAuth20Constants.CLIENT_ID + ']',
                     request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.CLIENT_ID, "invalid-client-id");
        assertFalse(validator.supports(request, response));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));
        assertEquals("Service [invalid-client-id] is not found or is not authorized for access",
                     request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));
        assertEquals("Missing required parameter: [" + OAuth20Constants.REDIRECT_URI + ']',
                     request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "invalid.uri");
        assertFalse(validator.supports(request, response));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));
        assertEquals("Redirect URI [invalid.uri] found in the request is not authorized for this service",
                     request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE, request.getAttribute(OAuth20Constants.ERROR).toString());
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));
        assertEquals("Missing required parameter: [" + OAuth20Constants.RESPONSE_TYPE + ']',
                     request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, "unknown");
        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE, request.getAttribute(OAuth20Constants.ERROR).toString());
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));
        assertEquals("Unsupported response_type: [unknown]",
                     request.getAttribute(OAuth20Constants.ERROR_DESCRIPTION));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        request.setParameter(OAuth20Constants.CODE_VERIFIER, "abcd");
        service.setSupportedResponseTypes(new LinkedHashSet<>());
        assertTrue(validator.supports(request, response));
        assertTrue(validator.validate(request, response));

        request.removeAttribute(OAuth20Constants.ERROR);
        assertTrue(validator.supports(request, response));
        assertTrue(validator.validate(request, response));
        assertNull(request.getAttribute(OAuth20Constants.ERROR));

        request.removeAttribute(OAuth20Constants.ERROR);
        service.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.CODE.getType()));
        assertTrue(validator.supports(request, response));
        assertTrue(validator.validate(request, response));
        assertNull(request.getAttribute(OAuth20Constants.ERROR));

        request.removeAttribute(OAuth20Constants.ERROR);
        service.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.TOKEN.getType()));
        assertTrue(validator.supports(request, response));
        assertFalse(validator.validate(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "unknown-uri");
        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());

        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
        assertFalse(validator.supports(request, response));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());

        assertEquals(Ordered.LOWEST_PRECEDENCE, validator.getOrder());
        assertNotNull(validator.getRegisteredServiceAccessStrategyEnforcer());
        assertTrue(validator.getSupportedResponseTypes().contains(OAuth20ResponseTypes.CODE));
        assertNotNull(validator.getServicesManager());
        assertNotNull(validator.getWebApplicationServiceServiceFactory());
    }
}
