package org.apereo.cas.validation;

/**
 * Enumerates the list of response types
 * that CAS may produce as a result of
 * service being validated.
 * @author Misagh Moayyed
 * @since 4.2
 */
public enum ValidationResponseType {
    /**
     * Default CAS XML response.
     */
    XML,
    /**
     * Render response in JSON.
     */
    JSON
}
