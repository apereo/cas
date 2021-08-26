package org.apereo.cas.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumerates the list of response types
 * that CAS may produce as a result of
 * service being validated.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RequiredArgsConstructor
@Getter
public enum ValidationResponseType {
    /**
     * Default CAS XML response.
     */
    XML(true),
    /**
     * Render response in JSON.
     */
    JSON(false);

    private final boolean encodingNecessary;
}
