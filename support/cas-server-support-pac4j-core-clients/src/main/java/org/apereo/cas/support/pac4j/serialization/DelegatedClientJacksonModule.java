package org.apereo.cas.support.pac4j.serialization;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oidc.profile.OidcProfile;

import java.io.IOException;

/**
 * This is {@link DelegatedClientJacksonModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class DelegatedClientJacksonModule extends SimpleModule {
    private static final long serialVersionUID = 4380897174293794761L;

    public DelegatedClientJacksonModule() {
        addSerializer(OidcProfile.class, new CommonProfileSerializer(OidcProfile.class));
    }

    @RequiredArgsConstructor
    private static class CommonProfileSerializer<T extends CommonProfile> extends JsonSerializer<T> {
        private final Class<T> typeToHandle;

        private void serialize(final T value, final JsonGenerator gen) throws IOException {
            val profile = EncodingUtils.encodeBase64(SerializationUtils.serialize(value));
            gen.writeString(profile);
        }

        @Override
        public void serialize(final T value, final JsonGenerator gen,
            final SerializerProvider serializerProvider) throws IOException {
            serialize(value, gen);
        }

        @Override
        public void serializeWithType(final T value, final JsonGenerator gen,
            final SerializerProvider serializers, final TypeSerializer typeSer)
            throws IOException {
            serialize(value, gen);
        }

        @Override
        public Class<T> handledType() {
            return typeToHandle;
        }
    }
}
