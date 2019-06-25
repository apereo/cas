package org.apereo.cas.consent;

import java.util.Arrays;

/**
 * This is {@link ConsentReminderOptions}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public enum ConsentReminderOptions {
    /**
     * Always ask for consent.
     */
    ALWAYS(0),
    /**
     * Ask for consent when there is modification in one of the attribute names or
     * if consent is expired.
     */
    ATTRIBUTE_NAME(1),
    /**
     * Ask for consent when there is modification in one of the attribute names,
     * the values contain inside the attributes or if consent is expired.
     */
    ATTRIBUTE_VALUE(2);

    private final int value;

    ConsentReminderOptions(final int value) {
        this.value = value;
    }

    /**
     * Value of consent options.
     *
     * @param value the value
     * @return the consent options
     */
    public static ConsentReminderOptions valueOf(final int value) {
        return Arrays.stream(values())
            .filter(v -> v.getValue() == value)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public int getValue() {
        return value;
    }
}
