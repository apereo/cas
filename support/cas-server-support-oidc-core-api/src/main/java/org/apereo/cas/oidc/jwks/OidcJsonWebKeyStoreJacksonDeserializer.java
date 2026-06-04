package org.apereo.cas.oidc.jwks;

import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jose4j.jwk.JsonWebKeySet;

/**
 * This is {@link OidcJsonWebKeyStoreJacksonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OidcJsonWebKeyStoreJacksonDeserializer extends JsonDeserializer<JsonWebKeySet> {

    @Override
    public JsonWebKeySet deserialize(final JsonParser jp, final DeserializationContext ctx) {
        return FunctionUtils.doUnchecked(() -> {
            val mapper = (ObjectMapper) jp.getCodec();
            val node = mapper.readValue(jp, JsonNode.class);
            rejectSpringExpressions(node);
            val json = mapper.writeValueAsString(node);
            return new JsonWebKeySet(json);
        });
    }

    private static void rejectSpringExpressions(final JsonNode node) {
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                rejectSpringExpression(entry.getKey());
                rejectSpringExpressions(entry.getValue());
            });
        } else if (node.isArray()) {
            node.elements().forEachRemaining(OidcJsonWebKeyStoreJacksonDeserializer::rejectSpringExpressions);
        } else if (node.isTextual()) {
            rejectSpringExpression(node.asText());
        }
    }

    private static void rejectSpringExpression(final String value) {
        if (Strings.CS.contains(value, "${") || Strings.CS.contains(value, "#{")) {
            throw new IllegalArgumentException("JWKS cannot contain expressions");
        }
    }
}
