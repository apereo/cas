package org.apereo.cas.oidc.claims.mapping;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultAttributeToScopeClaimMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcDefaultAttributeToScopeClaimMapperTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val mapper = new OidcDefaultAttributeToScopeClaimMapper(CollectionUtils.wrap("name", "givenName"));
        assertTrue(mapper.containsMappedAttribute("name"));
        assertEquals("givenName", mapper.getMappedAttribute("name"));
    }
}
