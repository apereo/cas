package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * This is {@link OidcJsonWebKeyStoreJacksonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OidcJsonWebKeyStoreJacksonDeserializer extends ValueDeserializer<JsonWebKeySet> {
    @Override
    public JsonWebKeySet deserialize(final JsonParser parser, final DeserializationContext ctxt) throws JacksonException {
        return FunctionUtils.doUnchecked(() -> {
            val node = ctxt.readTree(parser);
            return new JsonWebKeySet(node.toPrettyString());
        });
    }
}
