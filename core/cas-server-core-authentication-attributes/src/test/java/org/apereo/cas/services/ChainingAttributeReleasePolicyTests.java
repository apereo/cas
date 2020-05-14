package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class ChainingAttributeReleasePolicyTests {
    private ChainingAttributeReleasePolicy chain;

    @BeforeEach
    public void initialize() {
        configureChainingReleasePolicy(0, 0);
    }

    private void configureChainingReleasePolicy(final int order1, final int order2) {
        chain = new ChainingAttributeReleasePolicy();

        val p1 = new ReturnMappedAttributeReleasePolicy();
        p1.setOrder(order1);
        p1.setAllowedAttributes(CollectionUtils.wrap("givenName", "groovy {return ['CasUserPolicy1']}"));

        val p2 = new ReturnMappedAttributeReleasePolicy();
        p2.setOrder(order2);
        p2.setAllowedAttributes(CollectionUtils.wrap("givenName", "groovy {return ['CasUserPolicy2']}"));

        chain.addPolicies(p1, p2);
    }

    @Test
    public void verifyOperationWithReplaceAndOrder() {
        configureChainingReleasePolicy(10, 1);
        chain.setMergingPolicy("replace");
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy1", values.iterator().next().toString());
    }

    @Test
    public void verifyOperationWithReplace() {
        chain.setMergingPolicy("replace");
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy2", values.iterator().next().toString());
    }

    @Test
    public void verifyOperationWithAdd() {
        chain.setMergingPolicy("add");
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy1", values.iterator().next().toString());
    }

    @Test
    public void verifyOperationWithMultivalued() {
        chain.setMergingPolicy("multivalued");
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(2, values.size());
        assertTrue(values.contains("CasUserPolicy1"));
        assertTrue(values.contains("CasUserPolicy2"));
    }
}
