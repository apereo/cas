/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.x509.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
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

    /**
     * @see java.security.cert.X509CRL#getEncoded()
     */
    @Override
    public byte[] getEncoded() throws CRLException {
        return null;
    }

    /**
     * @see java.security.cert.X509CRL#getIssuerDN()
     */
    @Override
    public Principal getIssuerDN() {
        return this.issuer;
    }

    /**
     * @see java.security.cert.X509CRL#getNextUpdate()
     */
    @Override
    public Date getNextUpdate() {
        return this.nextUpdate;
    }

    /**
     * @see java.security.cert.X509CRL#getRevokedCertificate(java.math.BigInteger)
     */
    @Override
    public X509CRLEntry getRevokedCertificate(final BigInteger serialNumber) {
        return null;
    }

    /**
     * @see java.security.cert.X509CRL#getRevokedCertificates()
     */
    @Override
    public Set< ? extends X509CRLEntry> getRevokedCertificates() {
        return null;
    }

    /**
     * @see java.security.cert.X509CRL#getSigAlgName()
     */
    @Override
    public String getSigAlgName() {
        return "SHA1";
    }

    /**
     * @see java.security.cert.X509CRL#getSigAlgOID()
     */
    @Override
    public String getSigAlgOID() {
        return "1.3.14.3.2.26";
    }

    /**
     * @see java.security.cert.X509CRL#getSigAlgParams()
     */
    @Override
    public byte[] getSigAlgParams() {
        return null;
    }

    /**
     * @see java.security.cert.X509CRL#getSignature()
     */
    @Override
    public byte[] getSignature() {
        return null;
    }

    /**
     * @see java.security.cert.X509CRL#getTBSCertList()
     */
    @Override
    public byte[] getTBSCertList() throws CRLException {
        return null;
    }

    /**
     * @see java.security.cert.X509CRL#getThisUpdate()
     */
    @Override
    public Date getThisUpdate() {
        return this.thisUpdate;
    }

    /**
     * @see java.security.cert.X509CRL#getVersion()
     */
    @Override
    public int getVersion() {
        return 0;
    }

    /**
     * @see java.security.cert.X509CRL#verify(java.security.PublicKey)
     */
    @Override
    public void verify(final PublicKey key) throws CRLException,
    NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
    SignatureException {
        // Do nothing to indicate valid signature
    }

    /**
     * @see java.security.cert.X509CRL#verify(java.security.PublicKey, java.lang.String)
     */
    @Override
    public void verify(final PublicKey key, final String sigProvider) throws CRLException,
    NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
    SignatureException {
        // Do nothing to indicate valid signature
    }

    /**
     * @see java.security.cert.CRL#isRevoked(java.security.cert.Certificate)
     */
    @Override
    public boolean isRevoked(final Certificate cert) {
        if (cert instanceof X509Certificate) {
            final X509Certificate xcert = (X509Certificate) cert;
            for (final X509CRLEntry entry : getRevokedCertificates()) {
                if (entry.getSerialNumber().equals(xcert.getSerialNumber())) {
                    return true;
                }
            }
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
