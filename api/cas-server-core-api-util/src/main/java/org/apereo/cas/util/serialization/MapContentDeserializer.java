package org.apereo.cas.util.serialization;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * This is {@link MapContentDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class MapContentDeserializer extends ValueDeserializer {
    private final JavaType contentType;
    private final ValueDeserializer<Object> delegate;

    @Override
    public Object deserialize(final JsonParser parser, final DeserializationContext context) {
        val currentToken = parser.currentToken();
        if (currentToken == JsonToken.VALUE_NULL) {
            return getNullValue(context);
        }
        val value = delegate != null
            ? delegate.deserialize(parser, context)
            : context.readValue(parser, (contentType != null) ? contentType : context.constructType(Object.class));

        if (value instanceof CharSequence && (contentType == null || contentType.hasRawClass(String.class))) {
            return value.toString().trim();
        }
        return value;
    }

    @Override
    public ValueDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty property) {
        var containerType = ctxt.getContextualType();
        if (containerType == null && property != null) {
            containerType = property.getType();
        }
        
        val valueType = containerType != null && containerType.isContainerType()
            ? containerType.getContentType()
            : (this.contentType != null ? this.contentType : ctxt.constructType(Object.class));

        var valueDeser = property != null
            ? ctxt.findContextualValueDeserializer(valueType, property)
            : ctxt.findRootValueDeserializer(valueType);

        if (valueDeser == null) {
            valueDeser = ctxt.findNonContextualValueDeserializer(valueType);
        }

        return new MapContentDeserializer(valueType, valueDeser);
    }
}
