package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.attribute.SamlAttributeEncoder;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * This is {@link SamlAttributeEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlAttributeEncoderTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void verifyAction() {
        val encoder = new SamlAttributeEncoder();
        val original = CoreAuthenticationTestUtils.getAttributes();
        original.put("address", EncodingUtils.hexEncode("123 Main Street"));
        val attributes = encoder.encodeAttributes(original, CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals(original.size(), attributes.size());
        assertTrue(attributes.containsKey("address"));
    }

    @Test
    public void ensureSamlUrnAttributesEncoded() {
        val encoder = new SamlAttributeEncoder();
        val attributes = new HashMap<String, Object>();
        attributes.put(EncodingUtils.hexEncode("urn:oid:2.5.4.3"), "testValue");
        val result = encoder.encodeAttributes(attributes, CoreAuthenticationTestUtils.getRegisteredService("test"));
        assertTrue(result.containsKey("urn:oid:2.5.4.3"));
    }

    @Test
    public void ensureSamlMsftClaimsAttributesEncoded() {
        val encoder = new SamlAttributeEncoder();
        val attributes = new HashMap<String, Object>();
        attributes.put("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname", "testValue");
        val result = encoder.encodeAttributes(attributes, CoreAuthenticationTestUtils.getRegisteredService("test"));
        assertTrue(result.containsKey("http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname"));
    }
}
