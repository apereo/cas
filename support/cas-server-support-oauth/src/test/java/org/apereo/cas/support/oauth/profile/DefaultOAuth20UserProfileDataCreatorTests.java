package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.AbstractOAuth20Tests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20UserProfileDataCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class DefaultOAuth20UserProfileDataCreatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oAuth2UserProfileDataCreator")
    private OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        val map = oAuth2UserProfileDataCreator.createFrom(getAccessToken(), context);
        assertFalse(map.isEmpty());
    }

}
