package org.apereo.cas.configuration.model.core.slo;

/**
 * This is {@link SloProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SloProperties {
    
    private boolean asynchronous = true;
    private boolean disabled;

    public boolean isAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(final boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }
}
