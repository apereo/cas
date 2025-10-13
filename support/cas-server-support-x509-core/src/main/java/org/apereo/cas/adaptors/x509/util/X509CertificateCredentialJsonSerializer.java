package org.apereo.cas.adaptors.x509.util;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import lombok.val;
import org.jooq.lambda.Unchecked;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import java.util.Arrays;

/**
 * This is {@link X509CertificateCredentialJsonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class X509CertificateCredentialJsonSerializer extends ValueSerializer<X509CertificateCredential> {

    @Override
    public void serialize(final X509CertificateCredential value,
                          final JsonGenerator generator,
                          final SerializationContext context) {

        val withType = context.getAttribute("WithType") != null;

        if (withType) {
            generator.writeName("certificates");
            generator.writeStartArray();
        }
        Arrays.stream(value.getCertificates()).forEach(Unchecked.consumer(c -> generator.writeBinary(c.getEncoded())));
        if (withType) {
            generator.writeEndArray();
        }
    }

    @Override
    public void serializeWithType(final X509CertificateCredential value, final JsonGenerator generator,
                                  final SerializationContext context, final TypeSerializer typeSer) {
        try {
            context.setAttribute("WithType", Boolean.TRUE);
            typeSer.writeTypePrefix(generator, context, typeSer.typeId(value, JsonToken.START_OBJECT));
            serialize(value, generator, context);
            typeSer.writeTypeSuffix(generator, context, typeSer.typeId(value, JsonToken.START_OBJECT));
        } finally {
            context.setAttribute("WithType", Boolean.FALSE);
        }
    }

    @Override
    public Class<X509CertificateCredential> handledType() {
        return X509CertificateCredential.class;
    }
}
