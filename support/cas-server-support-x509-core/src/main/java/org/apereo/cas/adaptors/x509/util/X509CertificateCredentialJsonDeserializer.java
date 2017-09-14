package org.apereo.cas.adaptors.x509.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CertUtils;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link X509CertificateCredentialJsonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class X509CertificateCredentialJsonDeserializer extends JsonDeserializer<X509CertificateCredential> {

    @Override
    public X509CertificateCredential deserialize(final JsonParser jp, 
                                                 final DeserializationContext deserializationContext) 
            throws IOException {
        final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);

        final List<X509Certificate> certs = new ArrayList<>();
        node.findValues("certificates").forEach(n -> {
            final String cert = n.get(0).textValue();
            final byte[] data = EncodingUtils.decodeBase64(cert);
            certs.add(CertUtils.readCertificate(new InputStreamResource(new ByteArrayInputStream(data))));
        });
        final X509CertificateCredential c = new X509CertificateCredential(certs.toArray(new X509Certificate[] {}));
        return c;
    }
}
