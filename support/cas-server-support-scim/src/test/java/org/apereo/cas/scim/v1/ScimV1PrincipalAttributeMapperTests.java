package org.apereo.cas.scim.v1;

import com.unboundid.scim.data.Meta;
import com.unboundid.scim.data.Name;
import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.schema.CoreSchema;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

/**
 * This is {@link ScimV1PrincipalAttributeMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ScimV1PrincipalAttributeMapperTests {
    @Test
    public void verifyAction() throws Exception {
        final var user = new UserResource(CoreSchema.USER_DESCRIPTOR);
        user.setActive(true);
        user.setDisplayName("CASUser");
        user.setId("casuser");
        final var name = new Name("formatted", "family",
            "middle", "givenMame", "prefix", "prefix2");
        name.setGivenName("casuser");
        user.setName(name);
        final var meta = new Meta(new Date(),
            new Date(), new URI("http://localhost:8215"), "1");
        meta.setCreated(new Date());
        user.setMeta(meta);

        try {
            final var mapper = new ScimV1PrincipalAttributeMapper();
            mapper.map(user, CoreAuthenticationTestUtils.getPrincipal(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
