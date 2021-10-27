/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import junit.framework.TestCase;

public class AbstractX509CertificateTests extends TestCase {

    public static final X509Certificate VALID_CERTIFICATE = new CasX509Certificate(
        true);

    public static final X509Certificate INVALID_CERTIFICATE = new CasX509Certificate(
        false);

    protected static class CasX509Certificate extends X509Certificate {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -4449243195531417769L;

        private boolean valid;

        protected CasX509Certificate(final boolean valid) {
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
                    return "CN=Jasig,DC=jasig,DC=org";
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
                    return "CN=CAS,DC=jasig,DC=org";
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
            return "CasX509Certficate";
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
