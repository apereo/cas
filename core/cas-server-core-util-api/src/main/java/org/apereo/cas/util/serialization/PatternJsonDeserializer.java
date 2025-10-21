package org.apereo.cas.util.serialization;

import org.apereo.cas.util.RegexUtils;
import lombok.val;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import java.util.regex.Pattern;

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
