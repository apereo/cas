package org.apereo.cas.adaptors.x509.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.EncodingUtils;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * This is {@link X509CertificateCredentialJsonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class X509CertificateCredentialJsonDeserializer extends JsonDeserializer<X509CertificateCredential> {

    @Override
    public X509CertificateCredential deserialize(final JsonParser jp, final DeserializationContext deserializationContext) throws IOException {
        final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);

        final X509Certificate[] certs = node.findValues("certificates").stream()
                .map(n -> n.get(0).textValue())
                .map(EncodingUtils::decodeBase64)
                .map(data -> CertUtils.readCertificate(new InputStreamResource(new ByteArrayInputStream(data))))
                .toArray(X509Certificate[]::new);

        return new X509CertificateCredential(certs);
    }
}
