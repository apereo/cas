package org.apereo.cas.oidc.jwks;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;

import java.io.IOException;

/**
 * This is {@link JsonWebKeySetJacksonSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class JsonWebKeySetJacksonSerializer extends JsonDeserializer<JsonWebKeySet> {

    @Override
    public JsonWebKeySet deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        try {
            val mapper = (ObjectMapper) jp.getCodec();
            val node = mapper.readTree(jp);
            val json = mapper.writeValueAsString(node);
            return new JsonWebKeySet(json);
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }
}
