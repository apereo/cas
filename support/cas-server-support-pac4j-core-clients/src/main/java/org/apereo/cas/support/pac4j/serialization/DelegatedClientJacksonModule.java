package org.apereo.cas.support.pac4j.serialization;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
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
        addSerializer(OidcProfile.class, new CommonProfileSerializer<OidcProfile>(OidcProfile.class));
        addDeserializer(OidcProfile.class, new CommonProfileDeserializer<OidcProfile>(OidcProfile.class));
    }

    private static class CommonProfileSerializer<T extends CommonProfile> extends ToStringSerializerBase {
        private static final long serialVersionUID = 4143814451543166784L;

        public CommonProfileSerializer(Class<T> handledType) {
            super(handledType);
        }

        @Override
        public String valueToString(Object value) {
            val profile = SerializationUtils.serialize((T) value);
            return EncodingUtils.encodeBase64(profile);
        }
    }

    private static class CommonProfileDeserializer<T extends CommonProfile> extends FromStringDeserializer<T> {
        private static final long serialVersionUID = -91663841137716758L;

        public CommonProfileDeserializer(Class<T> vc) {
            super(vc);
        }

        @Override
        protected T _deserialize(String value, DeserializationContext ctxt) throws IOException {
            val profile = EncodingUtils.decodeBase64(value);
            return (T) SerializationUtils.deserialize(profile, handledType());
        }
    }
}
