package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.hjson.JsonValue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20DefaultUserProfileViewRendererFlatTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("OAuth")
@TestPropertySource(properties = "cas.authn.oauth.userProfileViewType=FLAT")
public class OAuth20DefaultUserProfileViewRendererFlatTests extends AbstractOAuth20Tests {

    @Autowired
    @Qualifier("oauthUserProfileViewRenderer")
    private OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer;

    @Test
    public void verifyNestedOption() {
        val map = CollectionUtils.wrap(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, "cas",
            OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES,
            CollectionUtils.wrap("email", "cas@example.org", "name", "Test"),
            "something", CollectionUtils.wrapList("something"));
        val json = oauthUserProfileViewRenderer.render((Map) map, mock(OAuth20AccessToken.class), new MockHttpServletResponse());
        assertNotNull(json.getBody());
        val value = JsonValue.readJSON(json.getBody().toString()).asObject();
        assertNotNull(value.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID));
        assertNotNull(value.get("email"));
        assertNotNull(value.get("name"));
        assertNull(value.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
    }

}
