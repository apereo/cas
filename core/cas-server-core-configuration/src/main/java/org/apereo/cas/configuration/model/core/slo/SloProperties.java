package org.apereo.cas.configuration.model.core.slo;

/**
 * This is {@link SloProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SloProperties {

    /**
     * Whether SLO callbacks should be done in an asynchronous manner via the HTTP client.
     * When true, CAS will not wait for the operation to fully complete and will resume control to carry on.
     */
    private boolean asynchronous = true;

    /**
     * Whether SLO should be entirely disabled globally for the CAS deployment.
     */
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
