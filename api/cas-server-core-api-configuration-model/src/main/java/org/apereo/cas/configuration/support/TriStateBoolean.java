package org.apereo.cas.configuration.support;

import module java.base;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

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

    @Nullable
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
    public @Nullable Boolean toBoolean() {
        return state;
    }

    @NoArgsConstructor
    public static class Deserializer extends ValueDeserializer<TriStateBoolean> {

        @Override
        public TriStateBoolean deserialize(final JsonParser jsonParser,
                                           final DeserializationContext deserializationContext) throws JacksonException {
            val value = jsonParser.getString();
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
