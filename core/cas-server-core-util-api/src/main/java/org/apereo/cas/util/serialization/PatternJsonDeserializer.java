package org.apereo.cas.util.serialization;

import module java.base;
import org.apereo.cas.util.RegexUtils;
import lombok.val;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * This is {@link PatternJsonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class PatternJsonDeserializer extends ValueDeserializer<Pattern> {
    @Override
    public Pattern deserialize(final JsonParser parser, final DeserializationContext context) throws JacksonException {
        val patternString = parser.getString();
        return RegexUtils.createPattern(patternString);
    }
}
