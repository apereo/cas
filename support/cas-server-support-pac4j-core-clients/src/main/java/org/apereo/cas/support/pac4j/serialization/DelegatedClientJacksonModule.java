package org.apereo.cas.support.pac4j.serialization;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.pac4j.core.profile.CommonProfile;

import java.io.IOException;
import java.io.Serial;

/**
 * This is {@link DelegatedClientJacksonModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class DelegatedClientJacksonModule extends SimpleModule {
    @Serial
    private static final long serialVersionUID = 4380897174293794761L;

    public DelegatedClientJacksonModule() {
        setMixInAnnotation(CommonProfile.class, CommonProfileMixin.class);
    }

    private abstract static class CommonProfileMixin extends CommonProfile {
        @JsonValue
        public String serialize() {
            return EncodingUtils.encodeBase64(SerializationUtils.serialize(this));
        }
    }
}
