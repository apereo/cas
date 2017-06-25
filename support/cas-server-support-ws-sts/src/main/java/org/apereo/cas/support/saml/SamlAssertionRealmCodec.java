package org.apereo.cas.support.saml;

import org.apache.cxf.sts.token.realm.SAMLRealmCodec;
import org.apache.wss4j.common.saml.SAMLKeyInfo;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

/**
 * This is {@link SamlAssertionRealmCodec}.
 * Parse the realm from a SAML assertion.
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlAssertionRealmCodec implements SAMLRealmCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlAssertionRealmCodec.class);

    private final String realm;
    private final boolean uppercase = true;

    public SamlAssertionRealmCodec(final String realm) {
        this.realm = realm;
    }

    @Override
    public String getRealmFromToken(final SamlAssertionWrapper assertion) {
        final SAMLKeyInfo ki = assertion.getSignatureKeyInfo();
        final X509Certificate[] certs = ki.getCerts();
        final String parsed = parseCNValue(certs[0].getSubjectX500Principal().getName());
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
        final int len = name.indexOf(',') > 0 ? name.indexOf(',') : name.length();
        String realm = name.substring(name.indexOf("CN=") + "CN=".length(), len);

        if (uppercase) {
            realm = realm.toUpperCase();
        }
        return realm;
    }
}
