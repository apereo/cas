package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationCandidateProfileTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
class DelegatedAuthenticationCandidateProfileTests {
    @Test
    void verifyOperation() {
        val profile = DelegatedAuthenticationCandidateProfile.builder()
            .attributes(CoreAuthenticationTestUtils.getAttributes())
            .id(UUID.randomUUID().toString())
            .key(UUID.randomUUID().toString())
            .linkedId("casuser")
            .build();
        val userProfile = profile.toUserProfile("CasClient");
        assertNotNull(userProfile.getId());
        assertNotNull(userProfile.getAttributes());
        assertNotNull(userProfile.getClientName());
        assertNotNull(userProfile.getLinkedId());
    }
}
