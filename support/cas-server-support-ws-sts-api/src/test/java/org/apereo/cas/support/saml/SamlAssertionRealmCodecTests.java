package org.apereo.cas.support.saml;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;

import lombok.val;
import org.apache.wss4j.common.saml.SAMLKeyInfo;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlAssertionRealmCodecTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlAssertionRealmCodecTests {
    @Test
    public void verifyOperation() {
        val codec = new SamlAssertionRealmCodec("CAS");
        val wrapper = mock(SamlAssertionWrapper.class);
        val keyInfo = mock(SAMLKeyInfo.class);
        when(keyInfo.getCerts()).thenReturn(new X509Certificate[] {new CasX509Certificate(true)});
        when(wrapper.getSignatureKeyInfo()).thenReturn(keyInfo);
        assertNotNull(codec.getRealmFromToken(wrapper));
    }
}
