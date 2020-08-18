package org.apereo.cas.support.oauth.services;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20AuthenticationServiceSelectionStrategyTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy strategy;

    @Test
    public void verifyNullService() {
        assertNotNull(strategy.resolveServiceFrom(mock(Service.class)));
    }

    @Test
    public void verifyGrantType() {
        val request = new MockHttpServletRequest();
        request.addHeader("X-" + CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL2);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        assertNotNull(strategy.resolveServiceFrom(RegisteredServiceTestUtils.getService("https://example.org?"
            + OAuth20Constants.CLIENT_ID + '=' + CLIENT_ID + '&'
            + OAuth20Constants.GRANT_TYPE + '=' + OAuth20GrantTypes.CLIENT_CREDENTIALS.getType())));
    }

}
