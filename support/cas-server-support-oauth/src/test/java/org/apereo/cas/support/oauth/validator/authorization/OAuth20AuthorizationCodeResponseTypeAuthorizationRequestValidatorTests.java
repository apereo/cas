package org.apereo.cas.support.oauth.validator.authorization;

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
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
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
        addRegisteredService(Set.of(), "client", UUID.randomUUID().toString(), "https://.+");
        val validator = getValidator(servicesManager);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val authnRequest = "eyJhbGciOiJub25lIn0.eyJzY29wZSI6Im9wZW5pZCIsInJlc3Bvbn"
                           + "NlX3R5cGUiOiJjb2RlIiwicmVkaXJlY3RfdXJpIjoiaHR0"
                           + "cHM6XC9cL3N0YWdpbmcuY2VydGlmaWNhdGlvbi5vcGVua"
                           + "WQubmV0XC90ZXN0XC9hXC9DQVNcL2Nhb"
                           + "GxiYWNrIiwic3RhdGUiOiJ2SU4xYjBZNENrIiwibm9uY2UiOiI"
                           + "xTjltcVBPOWZ0IiwiY2xpZW50X2lkIjoiY2xpZW50In0.";

        request.setParameter(OAuth20Constants.REQUEST, authnRequest);
        assertTrue(validator.supports(context));
        assertTrue(validator.validate(context));
    }

    @Test
    void verifyValidator() throws Throwable {
        val service = addRegisteredService("https://.+", UUID.randomUUID().toString());
        val validator = getValidator(servicesManager);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.INVALID_REQUEST, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        assertFalse(validator.supports(context));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.INVALID_REQUEST, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, "unknown");
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        request.setParameter(OAuth20Constants.CODE_VERIFIER, "abcd");
        service.setSupportedResponseTypes(new LinkedHashSet<>());
        assertTrue(validator.supports(context));
        assertTrue(validator.validate(context));

        request.removeAttribute(OAuth20Constants.ERROR);
        assertTrue(validator.supports(context));
        assertTrue(validator.validate(context));
        assertFalse(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAttribute(OAuth20Constants.ERROR);
        service.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.CODE.getType()));
        assertTrue(validator.supports(context));
        assertTrue(validator.validate(context));
        assertFalse(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAttribute(OAuth20Constants.ERROR);
        service.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.TOKEN.getType()));
        assertTrue(validator.supports(context));
        assertFalse(validator.validate(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "unknown-uri");
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.INVALID_REQUEST, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());

        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.INVALID_REQUEST, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());

        assertEquals(Ordered.LOWEST_PRECEDENCE, validator.getOrder());
        assertNotNull(validator.getRegisteredServiceAccessStrategyEnforcer());
        assertTrue(validator.getSupportedResponseTypes().contains(OAuth20ResponseTypes.CODE));
        assertNotNull(validator.getServicesManager());
        assertNotNull(validator.getWebApplicationServiceServiceFactory());
    }
}
