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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPAttributeDefinitionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAML2")
public class SamlIdPAttributeDefinitionTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;

    @Test
    public void verifyOperation() throws Exception {
        assertTrue(SamlIdPAttributeDefinitionCatalog.load()
            .allMatch(defn -> attributeDefinitionStore.locateAttributeDefinition(defn.getKey(), SamlIdPAttributeDefinition.class).isPresent()));
        val defn = attributeDefinitionStore.locateAttributeDefinition("eduPersonTargetedID").get();
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
