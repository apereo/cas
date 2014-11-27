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

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractX509CertificateTests {

    public static final X509Certificate VALID_CERTIFICATE = new CasX509Certificate(
            true);

    public static final X509Certificate INVALID_CERTIFICATE = new CasX509Certificate(
            false);

    protected static class CasX509Certificate extends X509Certificate {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -4449243195531417769L;

        private final boolean valid;

        protected CasX509Certificate(final boolean valid) {
            this.valid = valid;
        }

        @Override
        public void checkValidity() throws CertificateExpiredException,
        CertificateNotYetValidException {
            if (!this.valid) {
                throw new CertificateExpiredException();
            }
        }

        @Override
        public void checkValidity(final Date arg0)
                throws CertificateExpiredException, CertificateNotYetValidException {
            if (!this.valid) {
                throw new CertificateExpiredException();
            }
        }

        @Override
        public int getBasicConstraints() {
            return -1;
        }

        @Override
        public Principal getIssuerDN() {
            return new Principal(){

                @Override
                public String getName() {
                    return "CN=Jasig,DC=jasig,DC=org";
                }
            };
        }

        @Override
        public boolean[] getIssuerUniqueID() {
            return null;
        }

        @Override
        public boolean[] getKeyUsage() {
            return null;
        }

        @Override
        public Date getNotAfter() {
            return null;
        }

        @Override
        public Date getNotBefore() {
            return null;
        }

        @Override
        public BigInteger getSerialNumber() {
            return new BigInteger("500000");
        }

        @Override
        public String getSigAlgName() {
            return null;
        }

        @Override
        public String getSigAlgOID() {
            return null;
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
        public Principal getSubjectDN() {
            return new Principal(){

                @Override
                public String getName() {
                    return "CN=CAS,DC=jasig,DC=org";
                }

            };
        }

        @Override
        public boolean[] getSubjectUniqueID() {
            return null;
        }

        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        @Override
        public byte[] getExtensionValue(final String arg0) {
            return null;
        }

        @Override
        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        @Override
        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return null;
        }

        @Override
        public PublicKey getPublicKey() {
            return null;
        }

        @Override
        public String toString() {
            return "CasX509Certficate";
        }

        @Override
        public void verify(final PublicKey arg0, final String arg1)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException, SignatureException {
            // nothing to do right now
        }

        @Override
        public void verify(final PublicKey arg0) throws CertificateException,
        NoSuchAlgorithmException, InvalidKeyException,
        NoSuchProviderException, SignatureException {
            // nothing to do right now
        }
    }
}
