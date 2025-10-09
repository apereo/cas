package org.apereo.cas.util.serialization;

import tools.jackson.databind.deser.impl.ErrorThrowingDeserializer;

/**
 * This is {@link MapContentDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public final class MapContentDeserializer extends ErrorThrowingDeserializer {
    public MapContentDeserializer() {
        super(new NoClassDefFoundError("Unable to find class to deserialize"));
    }
}
