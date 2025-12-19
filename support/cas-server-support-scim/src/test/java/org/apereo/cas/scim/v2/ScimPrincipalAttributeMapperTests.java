package org.apereo.cas.scim.v2;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseScimTests;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.resources.complex.Name;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimPrincipalAttributeMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SCIM")
@TestPropertySource(properties = {
    "cas.scim.target=http://localhost:9666/scim/v2",
    "cas.scim.username=scim-user",
    "cas.scim.password=changeit",
    "cas.scim.oauth-token=mfh834bsd202usn10snf"
})
@EnabledIfListeningOnPort(port = 9666)
class ScimPrincipalAttributeMapperTests extends BaseScimTests {

    @Test
    void verifyAction() {
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

        assertDoesNotThrow(() ->
            scim2PrincipalAttributeMapper.forCreate(CoreAuthenticationTestUtils.getPrincipal(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
}
