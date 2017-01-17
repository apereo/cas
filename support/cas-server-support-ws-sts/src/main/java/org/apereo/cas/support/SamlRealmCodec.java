package org.apereo.cas.support;

import org.apache.cxf.sts.token.realm.SAMLRealmCodec;
import org.apache.wss4j.common.saml.SAMLKeyInfo;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

/**
 * This is {@link SamlRealmCodec}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlRealmCodec implements SAMLRealmCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlRealmCodec.class);
    
    private boolean uppercase = true;

    @Override
    public String getRealmFromToken(final SamlAssertionWrapper assertion) {
        final SAMLKeyInfo ki = assertion.getSignatureKeyInfo();
        final X509Certificate[] certs = ki.getCerts();
        final String realm = parseCNValue(certs[0].getSubjectX500Principal().getName());
        LOGGER.info("Realm parsed in certificate: " + realm);
        return realm;
    }

    private String parseCNValue(final String name) {
        final int len = name.indexOf(",") > 0 ? name.indexOf(",") : name.length();
        String realm = name.substring(name.indexOf("CN=") + 3, len);

        if (uppercase) {
            realm = realm.toUpperCase();
        }
        return realm;
    }

    public boolean isUppercase() {
        return uppercase;
    }

    public void setUppercase(final boolean uppercase) {
        this.uppercase = uppercase;
    }

}
