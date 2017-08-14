package org.apereo.cas.services;

/**
 * This is {@link OidcSubjectTypes}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
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

    public String getType() {
        return type;
    }
}
