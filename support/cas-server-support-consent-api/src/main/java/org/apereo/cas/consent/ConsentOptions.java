package org.apereo.cas.consent;

/**
 * This is {@link ConsentOptions}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public enum ConsentOptions {
    /** Always ask for consent. */
    ALWAYS(1),
    /** Always ask for consent. */
    ATTRIBUTE_NAME(1),
    /** Always ask for consent. */
    ATTRIBUTE_VALUE(2);

    private final int value;

    ConsentOptions(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
