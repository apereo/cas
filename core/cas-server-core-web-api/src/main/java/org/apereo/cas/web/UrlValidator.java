package org.apereo.cas.web;

/**
 * Define the behavior of an url validator.
 *
 * @author swoeste
 * @since 5.2.0
 */
public interface UrlValidator {

    /**
     * <p>Checks if a field has a valid url address.</p>
     * <p>
     * Note that the method calls #isValidAuthority()
     * which checks that the domain is valid.
     *
     * @param value The value validation is being performed on.  A {@code null} value is considered invalid.
     * @return true if the url is valid.
     */
    boolean isValid(String value);

    /**
     * <p>Checks if a field has a valid domain.</p>
     *
     * @param value the value
     * @return the boolean
     */
    boolean isValidDomain(String value);

}
