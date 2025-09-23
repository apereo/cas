package org.apereo.cas.configuration.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Strings;
import java.io.IOException;

/**
 * This is {@link TriStateBoolean}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public enum TriStateBoolean {
    /**
     * Constant to represent the true state.
     */
    TRUE(Boolean.TRUE),
    /**
     * Constant to represent the false state.
     */
    FALSE(Boolean.FALSE),
    /**
     * Constant to represent the undefined state.
     */
    UNDEFINED(null);

    private static final long serialVersionUID = -145819796564884951L;

    private final Boolean state;

    /**
     * From boolean.
     *
     * @param value the value
     * @return the tri state boolean
     */
    public static TriStateBoolean fromBoolean(final boolean value) {
        return value ? TriStateBoolean.TRUE : TriStateBoolean.FALSE;
    }

    /**
     * Is true.
     *
     * @return true/false
     */
    public boolean isTrue() {
        return Boolean.TRUE.equals(this.state);
    }

    /**
     * Is false.
     *
     * @return true/false
     */
    public boolean isFalse() {
        return Boolean.FALSE.equals(this.state);
    }

    /**
     * Is undefined.
     *
     * @return true/false
     */
    public boolean isUndefined() {
        return this.state == null;
    }

    /**
     * To boolean.
     *
     * @return true/false
     */
    public Boolean toBoolean() {
        return state;
    }

    @NoArgsConstructor
    public static class Deserializer extends JsonDeserializer<TriStateBoolean> {

        @Override
        public TriStateBoolean deserialize(final JsonParser jsonParser,
                                           final DeserializationContext deserializationContext) throws IOException {
            val value = jsonParser.getText();
            if (Strings.CI.equals(value, Boolean.TRUE.toString())) {
                return TriStateBoolean.TRUE;
            }
            if (Strings.CI.equals(value, Boolean.FALSE.toString())) {
                return TriStateBoolean.FALSE;
            }
            return TriStateBoolean.valueOf(value);
        }
    }
}
