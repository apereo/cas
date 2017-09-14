package org.apereo.cas.adaptors.x509.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.adaptors.x509.util.X509CertificateCredentialJsonDeserializer;
import org.apereo.cas.adaptors.x509.util.X509CertificateCredentialJsonSerializer;
import org.apereo.cas.authentication.AbstractCredential;

import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * An X.509 certificate credential.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@JsonSerialize(using = X509CertificateCredentialJsonSerializer.class)
@JsonDeserialize(using = X509CertificateCredentialJsonDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class X509CertificateCredential extends AbstractCredential {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = 631753409512746474L;

    /**
     * The collection of certificates sent with the request.
     */
    private final X509Certificate[] certificates;

    /**
     * The certificate that we actually use.
     */
    private X509Certificate certificate;

    /**
     * Instantiates a new x509 certificate credential.
     *
     * @param certificates the certificates
     */
    public X509CertificateCredential(final X509Certificate[] certificates) {
        this.certificates = Arrays.copyOf(certificates, certificates.length);
    }

    public X509Certificate[] getCertificates() {
        return Arrays.copyOf(this.certificates, this.certificates.length);
    }

    public void setCertificate(final X509Certificate certificate) {
        this.certificate = certificate;
    }

    public X509Certificate getCertificate() {
        return this.certificate;
    }

    @Override
    public String getId() {
        X509Certificate cert = null;
        if (this.certificate != null) {
            cert = this.certificate;
        } else if (this.certificates.length > 0) {
            cert = this.certificates[0];
        }

        if (cert != null) {
            return CertUtils.toString(cert);
        }
        return UNKNOWN_ID;
    }
}
