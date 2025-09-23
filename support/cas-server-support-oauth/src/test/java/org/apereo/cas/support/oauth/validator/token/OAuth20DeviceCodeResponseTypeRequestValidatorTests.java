package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceCodeResponseTypeRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20DeviceCodeResponseTypeRequestValidatorTests extends AbstractOAuth20Tests {

    @Test
    void verifySupports() {
        val service = addRegisteredService();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val validator = new OAuth20DeviceCodeResponseTypeRequestValidator(servicesManager, serviceFactory, oauthRequestParameterResolver);
        val context = new JEEContext(request, response);
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertTrue(validator.supports(context));
        assertNotNull(validator.getServicesManager());
        assertEquals(Ordered.LOWEST_PRECEDENCE, validator.getOrder());
        assertNotNull(validator.getWebApplicationServiceServiceFactory());
    }

    @Test
    void verifyValidate() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val validator = new OAuth20DeviceCodeResponseTypeRequestValidator(servicesManager, serviceFactory, oauthRequestParameterResolver);
        val context = new JEEContext(request, response);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, "unknown");
        request.addParameter(OAuth20Constants.CLIENT_ID, "unknown");
        assertFalse(validator.validate(context));

        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        assertFalse(validator.validate(context));

        val service = addRegisteredService();
        request.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        assertTrue(validator.validate(context));
    }

    
}
