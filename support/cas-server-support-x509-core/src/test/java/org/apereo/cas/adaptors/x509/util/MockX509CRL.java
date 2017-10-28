package org.apereo.cas.adaptors.x509.util;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;


/**
 * Mock implementation of X.509 CRL.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class MockX509CRL extends X509CRL {
    /** Issuer name */
    private final X500Principal issuer;

    /** Instant CRL was issued. */
    private final Date thisUpdate;

    /** Instant on which next CRL update expected. */
    private final Date nextUpdate;

    /**
     * Creates a new instance with given parameters.
     *
     * @param issuer CRL issuer.
     * @param thisUpdate Instant CRL was issued.
     * @param nextUpdate Instant where next CRL update is expected.
     */
    public MockX509CRL(final X500Principal issuer, final Date thisUpdate, final Date nextUpdate) {
        this.issuer = issuer;
        this.thisUpdate = thisUpdate;
        this.nextUpdate = nextUpdate;
    }

    /**
     * @see java.security.cert.X509Extension#getCriticalExtensionOIDs()
     */
    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }

    /**
     * @see java.security.cert.X509Extension#getExtensionValue(java.lang.String)
     */
    @Override
    public byte[] getExtensionValue(final String oid) {
        return null;
    }

    /**
     * @see java.security.cert.X509Extension#getNonCriticalExtensionOIDs()
     */
    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }

    /**
     * @see java.security.cert.X509Extension#hasUnsupportedCriticalExtension()
     */
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
    public Date getNextUpdate() {
        return this.nextUpdate;
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
        // Do nothing to indicate valid signature
    }

    @Override
    public void verify(final PublicKey key, final String sigProvider) {
        // Do nothing to indicate valid signature
    }

    /**
     * @see java.security.cert.CRL#isRevoked(java.security.cert.Certificate)
     */
    @Override
    public boolean isRevoked(final Certificate cert) {
        if (cert instanceof X509Certificate) {
            final X509Certificate xcert = (X509Certificate) cert;
            return getRevokedCertificates().stream().anyMatch(entry -> entry.getSerialNumber().equals(xcert.getSerialNumber()));
        }
        return false;
    }

    /**
     * @see java.security.cert.CRL#toString()
     */
    @Override
    public String toString() {
        return "MockX509CRL for " + this.issuer;
    }

}
