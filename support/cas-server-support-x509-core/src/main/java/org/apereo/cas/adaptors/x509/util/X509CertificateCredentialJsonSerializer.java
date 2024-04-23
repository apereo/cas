package org.apereo.cas.adaptors.x509.util;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.jooq.lambda.Unchecked;

import java.io.IOException;
import java.util.Arrays;

/**
 * This is {@link X509CertificateCredentialJsonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class X509CertificateCredentialJsonSerializer extends JsonSerializer<X509CertificateCredential> {

    @Override
    public void serialize(final X509CertificateCredential value,
                          final JsonGenerator generator, final SerializerProvider serializers)
        throws IOException {

        if (serializers.getAttribute("WithType") != null) {
            generator.writeArrayFieldStart("certificates");
        }
        Arrays.stream(value.getCertificates()).forEach(Unchecked.consumer(c -> generator.writeBinary(c.getEncoded())));
        if (serializers.getAttribute("WithType") != null) {
            generator.writeEndArray();
        }
    }

    @Override
    public void serializeWithType(final X509CertificateCredential value, final JsonGenerator generator,
                                  final SerializerProvider serializers, final TypeSerializer typeSer) throws IOException {
        try {
            serializers.setAttribute("WithType", Boolean.TRUE);
            typeSer.writeTypePrefix(generator, typeSer.typeId(value, JsonToken.START_OBJECT));
            serialize(value, generator, serializers);
            typeSer.writeTypeSuffix(generator, typeSer.typeId(value, JsonToken.START_OBJECT));
        } finally {
            serializers.setAttribute("WithType", Boolean.FALSE);
        }
    }

    @Override
    public Class<X509CertificateCredential> handledType() {
        return X509CertificateCredential.class;
    }
}
