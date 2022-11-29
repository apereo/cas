package org.apereo.cas.authentication;

/**
 * Credential with surrogate capabilities.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
public interface SurrogateCredential extends Credential {
    /**
     * Set the identifier.
     *
     * @param id the identifier
     */
    void setId(String id);

    /**
     * Get the surrogate username.
     *
     * @return the surrogate username
     */
    String getSurrogateUsername();

    /**
     * Set the surrogate username.
     *
     * @param surrogateUsername the surrogate username
     */
    void setSurrogateUsername(String surrogateUsername);
}
