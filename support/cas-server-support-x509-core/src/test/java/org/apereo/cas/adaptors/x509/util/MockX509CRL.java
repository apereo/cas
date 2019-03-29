package org.apereo.cas.adaptors.x509.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/**
 * Mock implementation of X.509 CRL.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@ToString
@Getter
@RequiredArgsConstructor
public class MockX509CRL extends X509CRL {

    /**
     * Issuer name
     */
    private final X500Principal issuer;

    /**
     * Instant CRL was issued.
     */
    private final Date thisUpdate;

    /**
     * Instant on which next CRL update expected.
     */
    private final Date nextUpdate;

    @Override
    public byte[] getExtensionValue(final String oid) {
        return null;
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        return false;
    }

    @Override
    public byte[] getEncoded() {
        return null;
    }

    @Override
    public Principal getIssuerDN() {
        return this.issuer;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public X509CRLEntry getRevokedCertificate(final BigInteger serialNumber) {
        return null;
    }

    @Override
    public Set<? extends X509CRLEntry> getRevokedCertificates() {
        return null;
    }

    @Override
    public String getSigAlgName() {
        return "SHA1";
    }

    @Override
    public String getSigAlgOID() {
        return "1.3.14.3.2.26";
    }

    @Override
    public byte[] getSigAlgParams() {
        return null;
    }

    @Override
    public byte[] getSignature() {
        return null;
    }

    @Override
    public byte[] getTBSCertList() {
        return null;
    }

    @Override
    public Date getThisUpdate() {
        return this.thisUpdate;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void verify(final PublicKey key) {

    }

    @Override
    public void verify(final PublicKey key, final String sigProvider) {
    }

    /**
     * @see java.security.cert.CRL#isRevoked(java.security.cert.Certificate)
     */
    @Override
    public boolean isRevoked(final Certificate cert) {
        if (cert instanceof X509Certificate) {
            val xcert = (X509Certificate) cert;
            return getRevokedCertificates().stream().anyMatch(entry -> entry.getSerialNumber().equals(xcert.getSerialNumber()));
        }
        return false;
    }
}
