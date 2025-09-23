package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.consent.CasConsentableAttribute;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.io.Serializable;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPConsentableAttributeBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLResponse")
@ImportAutoConfiguration(CasConsentCoreAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml7850",
    "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-definitions.json"
})
class SamlIdPConsentableAttributeBuilderTests extends BaseSamlIdPWebflowTests {
    private static final String VALUE = UUID.randomUUID().toString();
    @Autowired
    @Qualifier("samlIdPConsentableAttributeBuilder")
    private ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder;
    @Test
    void verifyDefnWithSamlXSString() {
        val value = mock(XSString.class);
        when(value.getValue()).thenReturn(VALUE);
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertEquals(VALUE, attribute.getValues().getFirst().toString());
    }

    @Test
    void verifyDefnWithObject() {
        val value = mock(Object.class);
        when(value.toString()).thenReturn(VALUE);
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertEquals(VALUE, attribute.getValues().getFirst().toString());
    }

    @Test
    void verifyDefnWithSamlXSUri() {
        val value = mock(XSURI.class);
        when(value.getURI()).thenReturn(VALUE);
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertEquals(VALUE, attribute.getValues().getFirst().toString());
    }

    @Test
    void verifyDefnWithSerializable() {
        val value = mock(Serializable.class);
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertInstanceOf(Serializable.class, attribute.getValues().getFirst());
    }

    @Test
    void verifyOperationByName() {
        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .values(CollectionUtils.wrapList("1", "2"))
                .build());
        assertNotNull(attribute.getFriendlyName());
    }

    @Test
    void verifyOperationByKey() {
        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("eduPersonPrincipalName")
                .values(CollectionUtils.wrapList("1", "2"))
                .build());
        assertEquals("eduPersonPrincipalName-FriendlyName", attribute.getFriendlyName());
    }

    @Test
    void verifyOperationNotFound() {
        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("not-found")
                .values(CollectionUtils.wrapList("1", "2"))
                .build());
        assertNull(attribute.getFriendlyName());
    }
}
