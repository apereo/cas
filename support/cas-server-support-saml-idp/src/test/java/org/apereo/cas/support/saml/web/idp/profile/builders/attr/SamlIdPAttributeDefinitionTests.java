package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionResolutionContext;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPAttributeDefinitionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAMLAttributes")
class SamlIdPAttributeDefinitionTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;

    @Test
    void verifyStoreIsLoaded() {
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("eduPersonUniqueId").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("organizationName").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("displayName").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("eduPersonEntitlement").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("eduPersonPrincipalName").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("eduPersonScopedAffiliation").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("mail").isPresent());
        assertTrue(attributeDefinitionStore.locateAttributeDefinition("givenName").isPresent());
        assertFalse(attributeDefinitionStore.locateAttributeDefinition("email").isPresent());
    }

    @Test
    void verifyEduPersonTargetedID() throws Throwable {
        val defn = attributeDefinitionStore.locateAttributeDefinition("eduPersonTargetedID", SamlIdPAttributeDefinition.class)
            .get().withSalt(UUID.randomUUID().toString());
        val values = defn.resolveAttributeValues(getAttributeDefinitionResolutionContext());
        assertEquals(1, values.size());
    }

    private static AttributeDefinitionResolutionContext getAttributeDefinitionResolutionContext() {
        return AttributeDefinitionResolutionContext.builder()
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .build();
    }
}
