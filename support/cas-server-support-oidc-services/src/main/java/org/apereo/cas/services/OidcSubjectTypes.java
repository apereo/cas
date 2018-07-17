package org.apereo.cas.services;

import lombok.Getter;

/**
 * This is {@link OidcSubjectTypes}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public enum OidcSubjectTypes {

    /**
     * Public subject type.
     */
    PUBLIC("public"),
    /**
     * Pairwise subject type.
     */
    PAIRWISE("pairwise");

    private final String type;

    OidcSubjectTypes(final String type) {
        this.type = type;
    }
}
