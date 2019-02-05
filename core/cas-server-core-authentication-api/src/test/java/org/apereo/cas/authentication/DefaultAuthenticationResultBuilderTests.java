package org.apereo.cas.authentication;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationResultBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultAuthenticationResultBuilderTests {
    @Test
    public void verifyAuthenticationResultBuildsPrincipals() {
        val builder = new DefaultAuthenticationResultBuilder();
        val p1 = CoreAuthenticationTestUtils.getPrincipal("casuser1", CollectionUtils.wrap("uid", "casuser1"));
        val p2 = CoreAuthenticationTestUtils.getPrincipal("casuser2", CollectionUtils.wrap("givenName", "CAS"));
        val authn1 = CoreAuthenticationTestUtils.getAuthentication(p1, CollectionUtils.wrap("authn1", "first"));
        val authn2 = CoreAuthenticationTestUtils.getAuthentication(p2, CollectionUtils.wrap("authn2", "second"));

        val result = builder.collect(authn1).collect(authn2).build(new DefaultPrincipalElectionStrategy());

        val authentication = result.getAuthentication();
        assertNotNull(authentication);
        val authnAttributes = authentication.getAttributes();
        assertTrue(authnAttributes.containsKey("authn1"));
        assertTrue(authnAttributes.containsKey("authn2"));

        val principal = authentication.getPrincipal();
        assertNotNull(principal);

        val attributes = principal.getAttributes();
        assertFalse(attributes.isEmpty());

        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertEquals(1, ((Collection) attributes.get("uid")).size());
        assertEquals(1, ((Collection) attributes.get("givenName")).size());
    }

    @Test
    public void verifyAuthenticationResultMergesPrincipalAttributes() {
        val builder = new DefaultAuthenticationResultBuilder();
        val p1 = CoreAuthenticationTestUtils.getPrincipal("casuser1", CollectionUtils.wrap("givenName", "CAS", "uid", "casuser1"));
        val p2 = CoreAuthenticationTestUtils.getPrincipal("casuser2", CollectionUtils.wrap("email", "cas@example.org",
            "givenName", "CAS SSO", "uid", "casuser2"));
        val authn1 = CoreAuthenticationTestUtils.getAuthentication(p1, CollectionUtils.wrap("authn", "test1"));
        val authn2 = CoreAuthenticationTestUtils.getAuthentication(p2, CollectionUtils.wrap("authn", "test2"));

        val result = builder.collect(authn1).collect(authn2).build(new DefaultPrincipalElectionStrategy());

        val authentication = result.getAuthentication();
        assertNotNull(authentication);
        val authnAttributes = authentication.getAttributes();
        assertTrue(authnAttributes.containsKey("authn"));
        assertEquals(2, ((Collection) authnAttributes.get("authn")).size());

        val principal = authentication.getPrincipal();
        assertNotNull(principal);

        val attributes = principal.getAttributes();
        assertFalse(attributes.isEmpty());

        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertEquals(2, ((Collection) attributes.get("uid")).size());
        assertEquals(2, ((Collection) attributes.get("givenName")).size());
        assertEquals(1, ((Collection) attributes.get("email")).size());
    }
}
