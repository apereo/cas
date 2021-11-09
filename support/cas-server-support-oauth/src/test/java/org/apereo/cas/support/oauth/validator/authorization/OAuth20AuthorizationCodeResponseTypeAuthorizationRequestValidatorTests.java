package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OAuth")
public class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests {
    private static ServicesManager getServicesManager(final StaticApplicationContext applicationContext) {
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(applicationContext))
            .applicationContext(applicationContext)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        return new DefaultServicesManager(context);
    }

    private static OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator getValidator(final ServicesManager serviceManager) {
        return new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(serviceManager,
            new WebApplicationServiceFactory(),
            new RegisteredServiceAccessStrategyAuditableEnforcer(new CasConfigurationProperties()));
    }

    private static OAuthRegisteredService buildRegisteredService(final ServicesManager serviceManager) {
        val service = new OAuthRegisteredService();
        service.setId(1000);
        service.setName("OAuth");
        service.setClientId("client");
        service.setClientSecret("secret");
        service.setServiceId("https://.+");
        serviceManager.save(service);
        return service;
    }

    @Test
    public void verifyUnsignedRequestParameter() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val serviceManager = getServicesManager(applicationContext);

        buildRegisteredService(serviceManager);

        val validator = getValidator(serviceManager);

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
    public void verifyValidator() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val serviceManager = getServicesManager(applicationContext);
        val service = buildRegisteredService(serviceManager);

        val validator = getValidator(serviceManager);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        assertFalse(validator.supports(context));

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.CLIENT_ID, "client");
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, "unknown");
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.UNSUPPORTED_RESPONSE_TYPE);

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
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        request.removeAttribute(OAuth20Constants.ERROR);
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());

        service.getAccessStrategy().setServiceAccessAllowed(false);
        assertFalse(validator.supports(context));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);

        assertEquals(Ordered.LOWEST_PRECEDENCE, validator.getOrder());
        assertNotNull(validator.getRegisteredServiceAccessStrategyEnforcer());
        assertEquals(OAuth20ResponseTypes.CODE, validator.getResponseType());
        assertNotNull(validator.getServicesManager());
        assertNotNull(validator.getWebApplicationServiceServiceFactory());
    }
}
