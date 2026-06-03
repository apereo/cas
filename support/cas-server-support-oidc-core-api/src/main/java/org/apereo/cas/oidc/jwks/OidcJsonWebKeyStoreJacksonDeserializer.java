package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKeySet;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
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
            rejectSpringExpressions(node);
            return new JsonWebKeySet(node.toPrettyString());
        });
    }

    private static void rejectSpringExpressions(final JsonNode node) {
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                rejectSpringExpression(entry.getKey());
                rejectSpringExpressions(entry.getValue());
            });
        } else if (node.isArray()) {
            node.values().forEach(OidcJsonWebKeyStoreJacksonDeserializer::rejectSpringExpressions);
        } else if (node.isString()) {
            rejectSpringExpression(node.asString());
        }
    }

    private static void rejectSpringExpression(final String value) {
        if (Strings.CS.contains(value, "${") || Strings.CS.contains(value, "#{")) {
            throw new IllegalArgumentException("JWKS cannot contain expressions");
        }
    }
}
