package org.apereo.cas.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link ServiceValidationViewTypes}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
public enum ServiceValidationViewTypes {
    /**
     * JSON view.
     */
    JSON("json");

    private final String type;
}
