package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.resources.complex.Name;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimV2PrincipalAttributeMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SCIM")
class ScimV2PrincipalAttributeMapperTests {
    @Test
    void verifyAction() throws Throwable {
        val user = new User();
        user.setActive(true);
        user.setDisplayName("CASUser");
        user.setId("casuser");
        val name = new Name();
        name.setGivenName("casuser");
        user.setName(name);
        val meta = new Meta();
        meta.setResourceType("User");
        meta.setCreated(LocalDateTime.now(Clock.systemUTC()));
        meta.setLocation("http://localhost:8218");
        user.setMeta(meta);

        assertDoesNotThrow(() -> {
            val mapper = new DefaultScimV2PrincipalAttributeMapper();
            mapper.map(user, CoreAuthenticationTestUtils.getPrincipal(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        });
    }
}
