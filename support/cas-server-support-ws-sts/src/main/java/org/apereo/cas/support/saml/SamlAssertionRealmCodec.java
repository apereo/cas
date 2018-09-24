package org.apereo.cas.support.saml;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.sts.token.realm.SAMLRealmCodec;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;

/**
 * This is {@link SamlAssertionRealmCodec}.
 * Parse the realm from a SAML assertion.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlAssertionRealmCodec implements SAMLRealmCodec {

    private final String realm;
    private final boolean uppercase = true;

    @Override
    public String getRealmFromToken(final SamlAssertionWrapper assertion) {
        val ki = assertion.getSignatureKeyInfo();
        val certs = ki.getCerts();
        val parsed = parseCNValue(certs[0].getSubjectX500Principal().getName());
        LOGGER.debug("Realm parsed from certificate CN of the SAML assertion: [{}]", parsed);
        if (parsed.equals(realm)) {
            return parsed;
        }
        LOGGER.warn("Retrieved realm from CN of SAML assertion certificate [{}] does not match the CAS realm [{}]. "
                + "Beware that realm mismatch does requires configuration to implement realm relationships or identity mapping",
            parsed, realm);
        return parsed;
    }

    private String parseCNValue(final String name) {
        val index = name.indexOf(',');
        val len = index > 0 ? index : name.length();
        var commonName = name.substring(name.indexOf("CN=") + "CN=".length(), len);
        if (uppercase) {
            commonName = commonName.toUpperCase();
        }
        return commonName;
    }
}
