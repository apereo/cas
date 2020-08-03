package org.apereo.cas.support.saml;

import org.apereo.cas.util.RegexUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.equals(parsed, realm)) {
            return parsed;
        }
        LOGGER.warn("Retrieved realm from CN of SAML assertion certificate [{}] does not match the CAS realm [{}]. "
                + "Beware that realm mismatch does requires configuration to implement realm relationships or identity mapping",
            parsed, realm);
        return parsed;
    }

    private String parseCNValue(final String name) {
        val matcher = RegexUtils.createPattern("cn=(\\w+)").matcher(name);
        if (matcher.find()) {
            val commonName = matcher.group(1);
            return uppercase ? commonName.toUpperCase() : commonName;
        }
        return null;
    }
}
