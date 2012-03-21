/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

/**
 * Describes a generic status condition.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class Status {
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
        return description;
    }
}
