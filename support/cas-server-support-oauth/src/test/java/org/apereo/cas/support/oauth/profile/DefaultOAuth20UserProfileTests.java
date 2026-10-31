package org.apereo.cas.support.oauth.profile;

import module java.base;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20UserProfileTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OAuth")
class DefaultOAuth20UserProfileTests {

    @Test
    void verifyIdentifierIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultOAuth20UserProfile(" "));
        val profile = new DefaultOAuth20UserProfile("casuser");
        assertThrows(IllegalArgumentException.class, () -> profile.setId(StringUtils.EMPTY));
        profile.setId("client");
        assertEquals("client", profile.getId());
        assertEquals(DefaultOAuth20UserProfile.class.getCanonicalName(), profile.getAuthenticationHandlerName());
        assertEquals("handler", new DefaultOAuth20UserProfile("casuser", "handler").getAuthenticationHandlerName());
    }

    @Test
    void verifyAttributes() {
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAttribute("empty", null);
        assertFalse(profile.containsAttribute("empty"));

        profile.addAttribute("cn", "cas");
        profile.addAttribute("cn", "apereo");
        assertEquals("apereo", profile.getAttribute("cn"));

        profile.addAttributes(Map.of("groups", List.of("a", "b")));
        profile.addAttribute("groups", List.of("b", "c"));
        assertEquals(List.of("a", "b", "c"), profile.getAttribute("groups"));

        val attributes = profile.getAttributes();
        attributes.clear();
        assertTrue(profile.containsAttribute("cn"));
    }

    @Test
    void verifyAuthenticationAttributesAndRoles() {
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAuthenticationAttributes(Map.of("authenticationDate", "now"));
        profile.addAuthenticationAttribute("ignored", null);
        assertEquals("now", profile.getAuthenticationAttribute("authenticationDate"));
        assertEquals(1, profile.getAuthenticationAttributes().size());

        profile.addRoles(List.of("openid", "profile"));
        val roles = profile.getRoles();
        roles.clear();
        assertEquals(Set.of("openid", "profile"), profile.getRoles());
    }

    @Test
    void verifySerialization() {
        val profile = new DefaultOAuth20UserProfile("casuser", "handler");
        profile.addAttribute("cn", "cas");
        profile.addAuthenticationAttribute("authenticationDate", "now");
        profile.addRoles(List.of("openid"));
        val result = SerializationUtils.deserialize(SerializationUtils.serialize(profile), OAuth20UserProfile.class);
        assertEquals(profile.getId(), result.getId());
        assertEquals(profile.getAttributes(), result.getAttributes());
        assertEquals(profile.getAuthenticationAttributes(), result.getAuthenticationAttributes());
        assertEquals(profile.getRoles(), result.getRoles());
        assertEquals("handler", result.getAuthenticationHandlerName());
    }
}
