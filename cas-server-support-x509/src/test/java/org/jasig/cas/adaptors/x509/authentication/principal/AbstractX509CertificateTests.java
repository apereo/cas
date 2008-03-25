/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import junit.framework.TestCase;

public class AbstractX509CertificateTests extends TestCase {

    public static final X509Certificate VALID_CERTIFICATE = new CasX509Certficate(
        true);

    public static final X509Certificate INVALID_CERTIFICATE = new CasX509Certficate(
        false);

    protected static class CasX509Certficate extends X509Certificate {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -4449243195531417769L;

        private boolean valid;

        protected CasX509Certficate(final boolean valid) {
            this.valid = valid;
        }

        public void checkValidity() throws CertificateExpiredException,
            CertificateNotYetValidException {
            if (!this.valid) {
                throw new CertificateExpiredException();
            }
        }

        public void checkValidity(Date arg0)
            throws CertificateExpiredException, CertificateNotYetValidException {
            if (!this.valid) {
                throw new CertificateExpiredException();
            }
        }

        public int getBasicConstraints() {
            return -1;
        }

        public Principal getIssuerDN() {
            return new Principal(){

                public String getName() {
                    return "JA-SIG";
                }
            };
        }

        public boolean[] getIssuerUniqueID() {
            return null;
        }

        public boolean[] getKeyUsage() {
            return null;
        }

        public Date getNotAfter() {
            return null;
        }

        public Date getNotBefore() {
            return null;
        }

        public BigInteger getSerialNumber() {
            return new BigInteger("500000");
        }

        public String getSigAlgName() {
            return null;
        }

        public String getSigAlgOID() {
            return null;
        }

        public byte[] getSigAlgParams() {
            return null;
        }

        public byte[] getSignature() {
            return null;
        }

        public Principal getSubjectDN() {
            return new Principal(){

                public String getName() {
                    return "subject";
                }

            };
        }

        public boolean[] getSubjectUniqueID() {
            return null;
        }

        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }

        public int getVersion() {
            return 0;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String arg0) {
            return null;
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return null;
        }

        public PublicKey getPublicKey() {
            return null;
        }

        public String toString() {
            return "JA-SIG";
        }

        public void verify(PublicKey arg0, String arg1)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException {
            // nothing to do right now
        }

        public void verify(PublicKey arg0) throws CertificateException,
            NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
            // nothing to do right now
        }
    }
}
