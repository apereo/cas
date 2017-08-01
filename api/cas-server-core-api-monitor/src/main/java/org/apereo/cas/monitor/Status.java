package org.apereo.cas.monitor;

/**
 * Describes a generic status condition.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class Status {

    /** Generic UNKNOWN status. */
    public static final Status UNKNOWN = new Status(StatusCode.UNKNOWN);

    /** Generic OK status. */
    public static final Status OK = new Status(StatusCode.OK);

    /** Generic INFO status. */
    public static final Status INFO = new Status(StatusCode.INFO);

    /** Generic WARN status. */
    public static final Status WARN = new Status(StatusCode.WARN);

    /** Generic ERROR status. */
    public static final Status ERROR = new Status(StatusCode.ERROR);

    /** Status code. */
    private final StatusCode code;

    /** Human-readable status description. */
    private final String description;

    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     *
     * @see #getCode()
     */
    public Status(final StatusCode code) {
        this(code, null);
    }

    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param desc Human-readable status description.
     *
     * @see #getCode()
     */
    public Status(final StatusCode code, final String desc) {
        this.code = code;
        this.description = desc;
    }

    /**
     * Gets the status code.
     *
     * @return Status code.
     */
    public StatusCode getCode() {
        return this.code;
    }

    /**
     * @return Human-readable description of status.
     */
    public String getDescription() {
        return this.description;
    }
}
