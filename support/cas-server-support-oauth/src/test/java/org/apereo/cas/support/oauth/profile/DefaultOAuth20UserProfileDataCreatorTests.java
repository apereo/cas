package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultOAuth20UserProfileDataCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class DefaultOAuth20UserProfileDataCreatorTests {

    @Nested
    class DefaultTests extends AbstractOAuth20Tests {
        @Autowired
        @Qualifier(OAuth20UserProfileDataCreator.BEAN_NAME)
        private OAuth20UserProfileDataCreator oauth2UserProfileDataCreator;

        @Test
        void verifyOperation() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
            val response = new MockHttpServletResponse();
            val context = new JEEContext(request, response);
            val map = oauth2UserProfileDataCreator.createFrom(getAccessToken(), context);
            assertFalse(map.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oauth.core.user-profile-view-type=FLAT",
        "cas.authn.attribute-repository.stub.attributes.uid=cas",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=some-value"
    })
    class StatelessTests extends AbstractOAuth20Tests {
        @Autowired
        @Qualifier(OAuth20UserProfileDataCreator.BEAN_NAME)
        private OAuth20UserProfileDataCreator oauth2UserProfileDataCreator;

        @Test
        void verifyOperation() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
            val response = new MockHttpServletResponse();
            val context = new JEEContext(request, response);
            val accessToken = getAccessToken();
            when(accessToken.getExpiresIn()).thenReturn(60L);
            when(accessToken.isStateless()).thenReturn(Boolean.TRUE);
            val map = oauth2UserProfileDataCreator.createFrom(accessToken, context);
            val attributes = (Map) map.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES);
            assertTrue(attributes.containsKey("uid"));
            assertTrue(attributes.containsKey("groupMembership"));
        }
    }

}
