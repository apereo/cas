package org.apereo.cas.support.oauth.profile;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * The authenticated subject of an OAuth or OpenID Connect request: the user who signed in,
 * or the client that authenticated at the token endpoint. It carries the identifier, the attributes,
 * the authentication attributes and roles (granted scopes, for bearer tokens) of the authentication,
 * and the name recorded as the authentication handler of the CAS authentication built from it.
 * <p>
 * Attributes with a {@code null} value are ignored, and collection values added under an attribute that
 * already holds a collection are merged into one list without duplicates.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OAuth20UserProfile extends Serializable {
    /**
     * The authentication handler name of the anonymous profile of the device code flow. The handler names are the
     * success keys of the CAS authentication built from the profile, and are audited as {@code authn_methods}.
     */
    String ANONYMOUS_PROFILE_HANDLER_NAME = "OAuth20AnonymousProfile";

    /**
     * The authentication handler name of profiles that client authentication, the password grant, access tokens and JWT
     * bearer assertions build.
     */
    String COMMON_PROFILE_HANDLER_NAME = "OAuth20CommonProfile";

    /**
     * The authentication handler name of clients that authenticate with a certificate.
     */
    String X509_PROFILE_HANDLER_NAME = "OAuth20X509Profile";

    /**
     * The authentication handler name of users that the login handshake authenticates.
     */
    String BASIC_PROFILE_HANDLER_NAME = "OAuth20BasicUserProfile";

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    String getId();

    /**
     * Sets the identifier.
     *
     * @param id the identifier, never blank
     */
    void setId(String id);

    /**
     * Gets a copy of the attributes.
     *
     * @return the attributes
     */
    Map<String, Object> getAttributes();

    /**
     * Gets an attribute.
     *
     * @param name the name
     * @return the attribute value, or null
     */
    @Nullable Object getAttribute(String name);

    /**
     * Whether the attribute is present.
     *
     * @param name the name
     * @return true if the attribute is present
     */
    boolean containsAttribute(String name);

    /**
     * Adds an attribute.
     *
     * @param name  the name
     * @param value the value; ignored when null
     */
    void addAttribute(String name, @Nullable Object value);

    /**
     * Adds attributes.
     *
     * @param attributes the attributes
     */
    default void addAttributes(final Map<String, ?> attributes) {
        attributes.forEach(this::addAttribute);
    }

    /**
     * Gets a copy of the authentication attributes.
     *
     * @return the authentication attributes
     */
    Map<String, Object> getAuthenticationAttributes();

    /**
     * Gets an authentication attribute.
     *
     * @param name the name
     * @return the attribute value, or null
     */
    @Nullable Object getAuthenticationAttribute(String name);

    /**
     * Gets a copy of the roles.
     *
     * @return the roles
     */
    Set<String> getRoles();

    /**
     * Adds roles.
     *
     * @param roles the roles
     */
    void addRoles(Collection<String> roles);

    /**
     * The name recorded as the successful authentication handler of the CAS authentication
     * that is built from this profile.
     *
     * @return the authentication handler name
     */
    String getAuthenticationHandlerName();
}
