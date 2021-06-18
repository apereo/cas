package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAddressScopeAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcAddressScopeAttributeReleasePolicyTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val policy = new OidcAddressScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.ADDRESS.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedNormalClaims());

        val principal = CoreAuthenticationTestUtils.getPrincipal(
                CollectionUtils.wrap(
                        "name", List.of("cas"),
                        "street_address", "Main St",
                        "postal_code", "123456",
                        "locality", "Somewhere",
                        "region", "Earth",
                        "country", "Wonderland",
                        "formatted", "Some formatted address",
                        "attributeThatIsNotInAddressScope", "something"));
        val releasedAttributes = policy.getAttributes(principal,
                CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(releasedAttributes);

        val addressAttribute = releasedAttributes.get("address");
        assertNotNull(addressAttribute);

        val fieldMap = (Map<String, List<Object>>)addressAttribute.get(0);
        assertNotNull(fieldMap);

        assertFalse(fieldMap.containsKey("attributeThatIsNotInAddressScope"));
        assertEquals("Main St", fieldMap.get("street_address").get(0));
        assertEquals("123456", fieldMap.get("postal_code").get(0));
        assertEquals("Somewhere", fieldMap.get("locality").get(0));
        assertEquals("Wonderland", fieldMap.get("country").get(0));
        assertEquals("Earth", fieldMap.get("region").get(0));
        assertEquals("Some formatted address", fieldMap.get("formatted").get(0));
    }

    @Test
    public void verifySerialization() {
        val policy = new OidcAddressScopeAttributeReleasePolicy();
        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicy(policy);
        val service = getOidcRegisteredService();
        service.setAttributeReleasePolicy(chain);
        val serializer = new RegisteredServiceJsonSerializer();
        val json = serializer.toString(service);
        assertNotNull(json);
        assertNotNull(serializer.from(json));
    }
}
