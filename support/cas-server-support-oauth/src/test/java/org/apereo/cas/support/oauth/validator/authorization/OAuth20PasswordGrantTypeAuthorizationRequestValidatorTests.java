package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.pac4j.core.context.J2EContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.LinkedHashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20PasswordGrantTypeAuthorizationRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OAuth20PasswordGrantTypeAuthorizationRequestValidatorTests {
    @Test
    public void verifyValidator() {
        final ServicesManager serviceManager = mock(ServicesManager.class);
        final OAuthRegisteredService service = new OAuthRegisteredService();
        service.setName("OAuth");
        service.setClientId("client");
        service.setClientSecret("secret");
        service.setServiceId("https://callback.example.org");
        service.setSupportedGrantTypes(
                CollectionUtils.wrapHashSet(OAuth20GrantTypes.PASSWORD.getType()));

        when(serviceManager.getAllServices()).thenReturn(CollectionUtils.wrapList(service));
        final OAuth20PasswordGrantTypeAuthorizationRequestValidator v =
            new OAuth20PasswordGrantTypeAuthorizationRequestValidator(serviceManager, new WebApplicationServiceFactory(),
                new RegisteredServiceAccessStrategyAuditableEnforcer());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(v.validate(new J2EContext(request, response)));

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        assertFalse(v.validate(new J2EContext(request, response)));

        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        assertFalse(v.validate(new J2EContext(request, response)));

        request.addParameter(OAuth20Constants.SECRET, "secret");
        assertFalse(v.validate(new J2EContext(request, response)));

        request.addParameter(OAuth20Constants.USERNAME, "username");
        assertFalse(v.validate(new J2EContext(request, response)));

        request.addParameter(OAuth20Constants.PASSWORD, "password");

        service.setSupportedGrantTypes(new LinkedHashSet<>());
        assertTrue(v.validate(new J2EContext(request, response)));

        service.setSupportedGrantTypes(CollectionUtils.wrapHashSet(OAuth20GrantTypes.PASSWORD.getType()));
        assertTrue(v.validate(new J2EContext(request, response)));

        service.setSupportedGrantTypes(CollectionUtils.wrapHashSet(OAuth20GrantTypes.REFRESH_TOKEN.getType()));
        assertFalse(v.validate(new J2EContext(request, response)));

        assertTrue(v.supports(new J2EContext(request, response)));
    }
}
