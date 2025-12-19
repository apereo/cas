package org.apereo.cas.adaptors.x509.util;

import module java.base;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CertUtils;
import lombok.val;
import org.springframework.core.io.InputStreamResource;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import java.security.cert.X509Certificate;

/**
 * This is {@link X509CertificateCredentialJsonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class X509CertificateCredentialJsonDeserializer extends ValueDeserializer<X509CertificateCredential> {

    @Override
    public X509CertificateCredential deserialize(final JsonParser jsonParser,
                                                 final DeserializationContext deserializationContext) {
        val node = deserializationContext.readTree(jsonParser);
        val certificates = node.findValues("certificates");
        val certs = new ArrayList<X509Certificate>(certificates.size());
        certificates.forEach(certNode -> {
            val cert = certNode.get(0).stringValue();
            val data = EncodingUtils.decodeBase64(cert);
            certs.add(CertUtils.readCertificate(new InputStreamResource(new ByteArrayInputStream(data))));
        });
        return new X509CertificateCredential(certs.toArray(X509Certificate[]::new));
    }
}
