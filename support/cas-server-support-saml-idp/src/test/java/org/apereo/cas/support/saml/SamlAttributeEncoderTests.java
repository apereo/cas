package org.apereo.cas.support.saml;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.apereo.cas.util.EncodingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link SamlAttributeEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@Slf4j
public class SamlAttributeEncoderTests {

    @Test
    public void verifyAction() {
        final var encoder = new SamlAttributeEncoder();
        final var original = CoreAuthenticationTestUtils.getAttributes();
        original.put("address", EncodingUtils.hexEncode("123 Main Street"));
        final var attributes = encoder.encodeAttributes(original, CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals(original.size(), attributes.size());
        assertTrue(attributes.containsKey("address"));
    }

    @Test
    public void ensureSamlUrnAttributesEncoded() {
        final var encoder = new SamlAttributeEncoder();
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(EncodingUtils.hexEncode("urn:oid:2.5.4.3"), "testValue");
        final var result =
            encoder.encodeAttributes(attributes, CoreAuthenticationTestUtils.getRegisteredService("test"));
        assertTrue(result.containsKey("urn:oid:2.5.4.3"));
    }

    @Test
    public void ensureSamlMsftClaimsAttributesEncoded() {
        final var encoder = new SamlAttributeEncoder();
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname", "testValue");
        final var result = encoder.encodeAttributes(attributes, CoreAuthenticationTestUtils.getRegisteredService("test"));
        assertTrue(result.containsKey("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"));
    }
}
