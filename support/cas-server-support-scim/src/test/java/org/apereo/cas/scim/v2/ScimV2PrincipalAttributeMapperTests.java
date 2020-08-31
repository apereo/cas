package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.UserResource;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimV2PrincipalAttributeMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Attributes")
public class ScimV2PrincipalAttributeMapperTests {
    @Test
    public void verifyAction() throws Exception {
        val user = new UserResource();
        user.setActive(true);
        user.setDisplayName("CASUser");
        user.setId("casuser");
        val name = new Name();
        name.setGivenName("casuser");
        user.setName(name);
        val meta = new Meta();
        meta.setResourceType("User");
        meta.setCreated(Calendar.getInstance());
        meta.setLocation(new URI("http://localhost:8218"));
        user.setMeta(meta);

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val mapper = new ScimV2PrincipalAttributeMapper();
                mapper.map(user, CoreAuthenticationTestUtils.getPrincipal(),
                    CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            }
        });
    }
}
