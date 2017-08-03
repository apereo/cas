package org.apereo.cas.monitor;

/**
 * Monitor status code inspired by HTTP status codes.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public enum StatusCode {
    
    /** The error. */
    ERROR(500),
    
    /** The warn. */
    WARN(400),
    
    /** The info. */
    INFO(300),
    
    /** The ok. */
    OK(200),
    
    /** The unknown. */
    UNKNOWN(100);

    /** Status code numerical value. */
    private final int value;


    /**
     * Creates a new instance with the given numeric value.
     *
     * @param numericValue Numeric status code value.
     */
    StatusCode(final int numericValue) {
        this.value = numericValue;
    }


    /**
     * Gets the numeric value of the status code.  Higher values describe more severe conditions.
     *
     * @return Numeric status code value.
     */
    public int value() {
        return this.value;
    }
}
