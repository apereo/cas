package org.apereo.cas.scim.v2;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties.ScimUserSchema;
import org.apereo.cas.scim.v2.provisioning.DefaultScimPrincipalAttributeMapper;
import de.captaingoldfish.scim.sdk.common.constants.ResourceTypeNames;
import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultScimPrincipalAttributeMapperTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.5
 */
@Tag("SCIM")
class DefaultScimPrincipalAttributeMapperTests {
    private static final String USER_ID = "scim_user_01";

    private static final String USER_LOCATION = "https://scim.example.org/scim/v2/Users/" + USER_ID;

    @Test
    void verifyGroupMembersAreIdentifiedByValue() {
        val user = new User();
        user.setId(USER_ID);
        val meta = new Meta();
        meta.setLocation(USER_LOCATION);
        user.setMeta(meta);

        val groups = buildMapper().forGroups(buildPrincipal(), user);
        assertEquals(2, groups.size());
        assertEquals("Engineering", groups.getFirst().getDisplayName().orElseThrow());
        assertEquals("Operations", groups.getLast().getDisplayName().orElseThrow());
        groups.forEach(group -> {
            assertEquals(1, group.getMembers().size());
            val member = group.getMembers().getFirst();
            assertEquals(USER_ID, member.getValue().orElseThrow());
            assertEquals(USER_LOCATION, member.getRef().orElseThrow());
            assertEquals(ResourceTypeNames.USER, member.getType().orElseThrow());
            val json = group.toString();
            assertTrue(json.contains("\"value\":\"" + USER_ID + '"'), json);
        });
    }

    @Test
    void verifyGroupMembersWithoutLocation() {
        val user = new User();
        user.setId(USER_ID);

        val groups = buildMapper().forGroups(buildPrincipal(), user);
        assertEquals(2, groups.size());
        groups.forEach(group -> {
            val member = group.getMembers().getFirst();
            assertEquals(USER_ID, member.getValue().orElseThrow());
            assertTrue(member.getRef().isEmpty());
        });
    }

    private static DefaultScimPrincipalAttributeMapper buildMapper() {
        val properties = new ScimProvisioningProperties();
        properties.getSchemaMappings().put(ScimUserSchema.GROUPS.getName(), "memberships");
        return new DefaultScimPrincipalAttributeMapper(properties);
    }

    private static Principal buildPrincipal() {
        return CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("memberships", List.of("Engineering", "Operations")));
    }
}
