package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OAuth")
public class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests {
    @Test
    public void verifyValidator() {
        val serviceManager = mock(ServicesManager.class);

        val service = new OAuthRegisteredService();
        service.setName("OAuth");
        service.setClientId("client");
        service.setClientSecret("secret");
        service.setServiceId("https://callback.example.org");

        when(serviceManager.getAllServices()).thenReturn((Collection) CollectionUtils.toCollection(service));
        when(serviceManager.getAllServicesOfType(any())).thenReturn((Collection) CollectionUtils.toCollection(service));
        val v = new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(serviceManager, new WebApplicationServiceFactory(),
            new RegisteredServiceAccessStrategyAuditableEnforcer());

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        assertFalse(v.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        assertFalse(v.supports(context));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.CLIENT_ID, "client");
        assertFalse(v.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());
        assertFalse(v.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, "unknown");
        assertFalse(v.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        request.setParameter(OAuth20Constants.CODE_VERIFIER, "abcd");
        service.setSupportedResponseTypes(new LinkedHashSet<>());
        assertTrue(v.supports(context));
        assertTrue(v.validate(context));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REQUEST, "authn-request");
        assertTrue(v.supports(context));
        assertFalse(v.validate(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.REQUEST_NOT_SUPPORTED);

        request.removeParameter(OAuth20Constants.REQUEST);
        request.removeAttribute(OAuth20Constants.ERROR);
        assertTrue(v.supports(context));
        assertTrue(v.validate(context));
        assertFalse(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAttribute(OAuth20Constants.ERROR);
        service.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.CODE.getType()));
        assertTrue(v.supports(context));
        assertTrue(v.validate(context));
        assertFalse(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAttribute(OAuth20Constants.ERROR);
        service.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.TOKEN.getType()));
        assertTrue(v.supports(context));
        assertFalse(v.validate(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "unknown-uri");
        assertFalse(v.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());

        service.getAccessStrategy().setServiceAccessAllowed(false);
        assertFalse(v.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        assertEquals(Ordered.LOWEST_PRECEDENCE, v.getOrder());
        assertNotNull(v.getRegisteredServiceAccessStrategyEnforcer());
        assertEquals(OAuth20ResponseTypes.CODE, v.getResponseType());
        assertNotNull(v.getServicesManager());
        assertNotNull(v.getWebApplicationServiceServiceFactory());
    }
}
