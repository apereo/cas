package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20TokenResponseTypeAuthorizationRequestValidatorTests}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20TokenResponseTypeAuthorizationRequestValidatorTests {
    @Test
    public void verifySupports() {
        val serviceManager = mock(ServicesManager.class);

        val v = new OAuth20TokenResponseTypeAuthorizationRequestValidator(serviceManager, new WebApplicationServiceFactory(),
            new RegisteredServiceAccessStrategyAuditableEnforcer());

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        assertFalse(v.supports(context));

        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.getType());
        assertTrue(v.supports(context));
    }
}
