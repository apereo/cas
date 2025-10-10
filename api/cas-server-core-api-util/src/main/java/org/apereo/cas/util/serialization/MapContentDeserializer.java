package org.apereo.cas.util.serialization;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * This is {@link MapContentDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public final class MapContentDeserializer extends ValueDeserializer {
    @Override
    public Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws JacksonException {
        return null;
    }
}
