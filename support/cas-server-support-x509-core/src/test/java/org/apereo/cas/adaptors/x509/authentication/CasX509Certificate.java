package org.apereo.cas.adaptors.x509.authentication;

import org.apereo.cas.util.crypto.CertUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/**
 * This is {@link CasX509Certificate}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ToString
@RequiredArgsConstructor
public class CasX509Certificate extends X509Certificate {
    private static final long serialVersionUID = -4449243195531417769L;

    private final Resource certificateResource = new ClassPathResource("ldap-crl.crt");
    private final X509Certificate x509Certificate = CertUtils.readCertificate(certificateResource);

    private final boolean valid;

    @SneakyThrows
    public String getContent() {
        return IOUtils.toString(this.certificateResource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Override
    public void checkValidity() throws CertificateExpiredException {
        if (!this.valid) {
            throw new CertificateExpiredException();
        }
    }

    @Override
    public void checkValidity(final Date arg0) throws CertificateExpiredException {
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
    public void verify(final PublicKey arg0, final String arg1) {
    }

    @Override
    public void verify(final PublicKey arg0) {
    }
}
