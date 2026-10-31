package org.apereo.cas.uma.ticket.resource;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ResourceSetTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class ResourceSetTests {

    @Test
    void verifyOperation() {
        val set = new ResourceSet();
        assertThrows(InvalidResourceSetException.class,
            () -> set.validate(new DefaultOAuth20UserProfile(UUID.randomUUID().toString())));
        val profile = new DefaultOAuth20UserProfile(UUID.randomUUID().toString());
        assertThrows(InvalidResourceSetException.class,
            () -> set.validate(profile));
    }

    @Test
    void verifyClientAndOwnerMustMatch() {
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAttribute(OAuth20Constants.CLIENT_ID, "client-one");

        val set = new ResourceSet();
        set.setOwner(profile.getId());
        set.setClientId("client-one");
        set.setScopes(Set.of("read"));
        assertDoesNotThrow(() -> set.validate(profile));

        val otherClientProfile = new DefaultOAuth20UserProfile(profile.getId());
        otherClientProfile.addAttribute(OAuth20Constants.CLIENT_ID, "client-two");
        assertThrows(InvalidResourceSetException.class, () -> set.validate(otherClientProfile));
    }

}
