package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultOAuth20ProfileScopeToAttributesFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class DefaultOAuth20ProfileScopeToAttributesFilterTests extends AbstractOAuth20Tests {

    @Autowired
    @Qualifier("profileScopeToAttributesFilter")
    private OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    @Test
    public void verifyOperation() {
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val input = profileScopeToAttributesFilter.filter(
            RegisteredServiceTestUtils.getService(),
            principal,
            RegisteredServiceTestUtils.getRegisteredService(),
            new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse()),
            mock(OAuth20AccessToken.class));
        assertEquals(input, principal);
        assertTrue(profileScopeToAttributesFilter.getAttributeReleasePolicies().isEmpty());
    }

}
