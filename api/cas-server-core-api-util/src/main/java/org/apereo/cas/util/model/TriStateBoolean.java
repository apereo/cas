package org.apereo.cas.util.model;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * This is {@link TriStateBoolean}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public enum TriStateBoolean implements Serializable {
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

    public static TriStateBoolean fromBoolean(final boolean createCookieOnRenewedAuthentication) {
        return createCookieOnRenewedAuthentication ? TriStateBoolean.TRUE : TriStateBoolean.FALSE;
    }

    public boolean isTrue() {
        return Boolean.TRUE.equals(this.state);
    }

    public boolean isFalse() {
        return Boolean.FALSE.equals(this.state);
    }

    public boolean isUndefined() {
        return this.state == null;
    }

    public Boolean toBoolean() {
        return state;
    }
}
