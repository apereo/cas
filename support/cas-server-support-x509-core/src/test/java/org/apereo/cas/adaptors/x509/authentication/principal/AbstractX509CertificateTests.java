package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.util.crypto.CertUtils;
import org.springframework.core.io.ClassPathResource;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractX509CertificateTests extends AbstractCentralAuthenticationServiceTests {

    public static final X509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    protected static class CasX509Certificate extends X509Certificate {

        private static final long serialVersionUID = -4449243195531417769L;
        private final X509Certificate x509Certificate = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));

        private final boolean valid;

        protected CasX509Certificate(final boolean valid) {
            this.valid = valid;
        }


        @Override
        public void checkValidity() throws CertificateExpiredException {
            if (!this.valid) {
                throw new CertificateExpiredException();
            }
        }

        @Override
        public void checkValidity(final Date arg0)
                throws CertificateExpiredException {
            if (!this.valid) {
                throw new CertificateExpiredException();
            }
        }

        @Override
        public int getBasicConstraints() {
            return x509Certificate.getBasicConstraints();
        }

        @Override
        public Principal getIssuerDN() {
            return () -> "CN=Jasig,DC=jasig,DC=org";
        }

        @Override
        public boolean[] getIssuerUniqueID() {
            return x509Certificate.getIssuerUniqueID();
        }

        @Override
        public boolean[] getKeyUsage() {
            return x509Certificate.getKeyUsage();
        }

        @Override
        public Date getNotAfter() {
            return x509Certificate.getNotAfter();
        }

        @Override
        public Date getNotBefore() {
            return x509Certificate.getNotBefore();
        }

        @Override
        public BigInteger getSerialNumber() {
            return x509Certificate.getSerialNumber();
        }

        @Override
        public String getSigAlgName() {
            return x509Certificate.getSigAlgName();
        }

        @Override
        public String getSigAlgOID() {
            return x509Certificate.getSigAlgOID();
        }

        @Override
        public byte[] getSigAlgParams() {
            return x509Certificate.getSigAlgParams();
        }

        @Override
        public byte[] getSignature() {
            return x509Certificate.getSignature();
        }

        @Override
        public Principal getSubjectDN() {
            return x509Certificate.getSubjectDN();
        }

        @Override
        public boolean[] getSubjectUniqueID() {
            return x509Certificate.getSubjectUniqueID();
        }

        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return x509Certificate.getTBSCertificate();
        }

        @Override
        public int getVersion() {
            return x509Certificate.getVersion();
        }

        @Override
        public Set<String> getCriticalExtensionOIDs() {
            return x509Certificate.getCriticalExtensionOIDs();
        }

        @Override
        public byte[] getExtensionValue(final String arg0) {
            return x509Certificate.getExtensionValue(arg0);
        }

        @Override
        public Set<String> getNonCriticalExtensionOIDs() {
            return x509Certificate.getNonCriticalExtensionOIDs();
        }

        @Override
        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return x509Certificate.getEncoded();
        }

        @Override
        public PublicKey getPublicKey() {
            return x509Certificate.getPublicKey();
        }

        @Override
        public String toString() {
            return CertUtils.toString(x509Certificate);
        }

        @Override
        public void verify(final PublicKey arg0, final String arg1) {
            // nothing to do right now
        }

        @Override
        public void verify(final PublicKey arg0) {
            // nothing to do right now
        }
    }
}
