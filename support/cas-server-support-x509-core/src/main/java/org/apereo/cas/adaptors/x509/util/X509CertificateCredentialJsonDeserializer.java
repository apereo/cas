package org.apereo.cas.adaptors.x509.util;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CertUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * This is {@link X509CertificateCredentialJsonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class X509CertificateCredentialJsonDeserializer extends JsonDeserializer<X509CertificateCredential> {

    @Override
    public X509CertificateCredential deserialize(final JsonParser jp,
                                                 final DeserializationContext deserializationContext) throws IOException {
        val oc = jp.getCodec();
        val node = JsonNode.class.cast(oc.readTree(jp));

        val certificates = node.findValues("certificates");
        val certs = new ArrayList<X509Certificate>(certificates.size());
        certificates.forEach(n -> {
            val cert = n.get(0).textValue();
            val data = EncodingUtils.decodeBase64(cert);
            certs.add(CertUtils.readCertificate(new InputStreamResource(new ByteArrayInputStream(data))));
        });
        return new X509CertificateCredential(certs.toArray(X509Certificate[]::new));
    }
}
