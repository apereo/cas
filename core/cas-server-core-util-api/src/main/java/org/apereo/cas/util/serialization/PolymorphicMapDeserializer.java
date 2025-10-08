package org.apereo.cas.util.serialization;

import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PolymorphicMapDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class PolymorphicMapDeserializer extends ValueDeserializer<Map<String, ?>> {
    @Override
    public Map<String, ?> deserialize(final JsonParser parser, final DeserializationContext context) throws JacksonException {
        try {
            val mapNode = context.readTree(parser);
            val mapObjectNode = (ObjectNode) mapNode.deepCopy();
            mapObjectNode.remove("@class");

            val resultingMap = new LinkedHashMap<String, Object>();
            for (val nextNode : mapObjectNode.properties()) {
                val key = nextNode.getKey();
                var value = nextNode.getValue();

                if (value.isArray() && value.size() == 2 && value.get(0).isString() && value.get(1).isArray()) {
                    val containerType = value.get(0).asString();
                    val containerInstance = ClassUtils.getClass(containerType).getConstructor().newInstance();
                    if (containerInstance instanceof Collection collection) {
                        value = value.get(1);
                        for (val item : value) {
                            collection.add(item.asString());
                        }
                        resultingMap.put(key, containerInstance);
                    }
                }
                if (value.isObject()) {
                    val containerType = value.get("@class").asString();
                    val containerClass = ClassUtils.getClass(containerType);
                    val resultingObject = context.readTreeAsValue(value, containerClass);
                    resultingMap.put(key, resultingObject);
                }
                if (value.isString()) {
                    resultingMap.put(key, value.asString());
                }
            }
            return resultingMap;
        } catch (final Exception e) {
            throw DatabindException.from(parser, "Cannot deserialize polymorphic map", e);
        }
    }

    @Override
    public Object deserializeWithType(final JsonParser p, final DeserializationContext ctxt,
                                      final TypeDeserializer typeDeserializer) throws JacksonException {
        return deserialize(p, ctxt);
    }

}
