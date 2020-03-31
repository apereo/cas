package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.consent.CasConsentableAttribute;
import org.apereo.cas.consent.ConsentableAttributeBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPConsentableAttributeBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties =
    "cas.person-directory.attribute-definition-store.json.location=classpath:/basic-definitions.json")
public class SamlIdPConsentableAttributeBuilderTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("attributeDefinitionStore")
    private AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    @Qualifier("samlIdPConsentableAttributeBuilder")
    private ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder;

    @Test
    public void verifyOperationByName() {
        assertNotNull(samlIdPConsentableAttributeBuilder);
        assertNotNull(attributeDefinitionStore);

        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .values(List.of("1", "2"))
                .build());
        assertNotNull(attribute.getFriendlyName());
    }

    @Test
    public void verifyOperationByKey() {
        assertNotNull(samlIdPConsentableAttributeBuilder);
        assertNotNull(attributeDefinitionStore);

        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("eduPersonPrincipalName")
                .values(List.of("1", "2"))
                .build());
        assertNull(attribute.getFriendlyName());
    }

    @Test
    public void verifyOperationNotFound() {
        assertNotNull(samlIdPConsentableAttributeBuilder);
        assertNotNull(attributeDefinitionStore);

        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("not-found")
                .values(List.of("1", "2"))
                .build());
        assertNull(attribute.getFriendlyName());
    }
}
