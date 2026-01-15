package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlAttributeEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("SAML1")
class SamlAttributeEncoderTests {
    @Test
    void verifyAction() {
        val original = CoreAuthenticationTestUtils.getAttributes();
        original.put("address", EncodingUtils.hexEncode("123 Main Street"));
        val attributes = ProtocolAttributeEncoder.decodeAttributes(original, CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getWebApplicationService());
        assertEquals(original.size(), attributes.size());
        assertTrue(attributes.containsKey("address"));
    }

    @Test
    void ensureSamlUrnAttributesEncoded() {
        val attributes = new HashMap<String, Object>();
        attributes.put(ProtocolAttributeEncoder.encodeAttribute("urn:oid:2.5.4.3"), "testValue");
        val result = ProtocolAttributeEncoder.decodeAttributes(attributes, CoreAuthenticationTestUtils.getRegisteredService("test"),
            CoreAuthenticationTestUtils.getWebApplicationService());
        assertTrue(result.containsKey("urn:oid:2.5.4.3"));
    }

    @Test
    void ensureSamlMsftClaimsAttributesEncoded() {
        val attributes = new HashMap<String, Object>();
        attributes.put("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname", "testValue");
        val result = ProtocolAttributeEncoder.decodeAttributes(attributes, CoreAuthenticationTestUtils.getRegisteredService("test"),
            CoreAuthenticationTestUtils.getWebApplicationService());
        assertTrue(result.containsKey("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"));
    }
}
