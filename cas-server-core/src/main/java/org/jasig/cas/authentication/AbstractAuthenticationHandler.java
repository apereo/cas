package org.jasig.cas.authentication;

/**
 * Base class for all authentication handlers that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    /** Configurable handler name. */
    private String name;

    @Override
    public String getName() {
        return this.name != null ? this.name : getClass().getSimpleName();
    }

    /**
     * Sets the authentication handler name. Authentication handler names SHOULD be unique within an
     * {@link org.jasig.cas.authentication.AuthenticationManager}, and particular implementations may require uniqueness. Uniqueness is a best
     * practice generally.
     *
     * @param name Handler name.
     */
    public void setName(final String name) {
        this.name = name;
    }
}
