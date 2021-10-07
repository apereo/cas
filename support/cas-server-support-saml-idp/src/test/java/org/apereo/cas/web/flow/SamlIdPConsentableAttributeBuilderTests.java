package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasConsentCoreConfiguration;
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
import org.springframework.context.annotation.Import;
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
@Tag("SAML")
@Import(CasConsentCoreConfiguration.class)
@TestPropertySource(properties =
    "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-definitions.json")
public class SamlIdPConsentableAttributeBuilderTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPConsentableAttributeBuilder")
    private ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder;

    @Test
    public void verifyDefnWithSamlXSString() {
        val value = mock(XSString.class);
        when(value.getValue()).thenReturn(UUID.randomUUID().toString());
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertEquals(value.getValue(), attribute.getValues().get(0).toString());
    }

    @Test
    public void verifyDefnWithObject() {
        val value = mock(Object.class);
        when(value.toString()).thenReturn(UUID.randomUUID().toString());
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertEquals(value.toString(), attribute.getValues().get(0).toString());
    }

    @Test
    public void verifyDefnWithSamlXSUri() {
        val value = mock(XSURI.class);
        when(value.getURI()).thenReturn(UUID.randomUUID().toString());
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertEquals(value.getURI(), attribute.getValues().get(0).toString());
    }

    @Test
    public void verifyDefnWithSerializable() {
        val value = mock(Serializable.class);
        val attribute = samlIdPConsentableAttributeBuilder.build(CasConsentableAttribute.builder()
            .name("unknown")
            .values(CollectionUtils.wrapList(value))
            .build());
        assertTrue(attribute.getValues().get(0) instanceof Serializable);
    }

    @Test
    public void verifyOperationByName() {
        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
                .values(CollectionUtils.wrapList("1", "2"))
                .build());
        assertNotNull(attribute.getFriendlyName());
    }

    @Test
    public void verifyOperationByKey() {
        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("eduPersonPrincipalName")
                .values(CollectionUtils.wrapList("1", "2"))
                .build());
        assertNull(attribute.getFriendlyName());
    }

    @Test
    public void verifyOperationNotFound() {
        val attribute = samlIdPConsentableAttributeBuilder.build(
            CasConsentableAttribute.builder()
                .name("not-found")
                .values(CollectionUtils.wrapList("1", "2"))
                .build());
        assertNull(attribute.getFriendlyName());
    }
}
