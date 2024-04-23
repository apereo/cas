package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultOAuth20ProfileScopeToAttributesFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class DefaultOAuth20ProfileScopeToAttributesFilterTests extends AbstractOAuth20Tests {

    @Autowired
    @Qualifier(OAuth20ProfileScopeToAttributesFilter.BEAN_NAME)
    private OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    @Test
    void verifyOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val input = profileScopeToAttributesFilter.filter(
            RegisteredServiceTestUtils.getService(),
            principal,
            RegisteredServiceTestUtils.getRegisteredService(),
            mock(OAuth20AccessToken.class));
        assertEquals(input, principal);
        assertTrue(profileScopeToAttributesFilter.getAttributeReleasePolicies().isEmpty());
    }

}
