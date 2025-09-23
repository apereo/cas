package org.apereo.cas.util.serialization;

import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.val;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * This is {@link PatternJsonDeserializer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class PatternJsonDeserializer extends JsonDeserializer<Pattern> {
    @Override
    public Pattern deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        val patternString = parser.getText();
        return RegexUtils.createPattern(patternString);
    }
}
