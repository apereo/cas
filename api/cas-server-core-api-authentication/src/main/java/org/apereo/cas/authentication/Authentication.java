package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.principal.Principal;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * The Authentication object represents a successful authentication request. It
 * contains the principal that the authentication request was made for as well
 * as the additional meta information such as the authenticated date and a map
 * of attributes.
 * </p>
 * <p>
 * An Authentication object must be serializable to permit persistence and
 * clustering.
 * </p>
 * <p>
 * Implementing classes must take care to ensure that the Map returned by
 * getAttributes is serializable by using a Serializable map such as HashMap.
 * </p>
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface Authentication extends Serializable {

    /**
     * Method to obtain the Principal.
     *
     * @return a Principal implementation
     */
    Principal getPrincipal();

    /**
     * Method to retrieve the timestamp of when this Authentication object was
     * created.
     *
     * @return the date/time the authentication occurred.
     */
    ZonedDateTime getAuthenticationDate();

    /**
     * Attributes of the authentication (not the Principal).
     *
     * @return the map of attributes.
     */
    Map<String, Object> getAttributes();


    /**
     * Add attribute to the authentication object and update the instance.
     *
     * @param name  the name
     * @param value the value
     */
    void addAttribute(String name, Object value);

    /**
     * Gets a list of metadata about the credentials supplied at authentication time.
     *
     * @return Non -null list of supplied credentials represented as metadata that should be
     * considered safe for long-term storage (e.g. serializable and secure with respect to credential disclosure).
     * The order of items in the returned list SHOULD be the same as the order in which the source credentials
     * were presented and subsequently processed.
     */
    List<CredentialMetaData> getCredentials();

    /**
     * Gets a map describing successful authentications produced by {@link AuthenticationHandler} components.
     *
     * @return Map of handler names to successful authentication result produced by that handler.
     */
    Map<String, HandlerResult> getSuccesses();

    /**
     * Gets a map describing failed authentications. By definition the failures here were not sufficient to prevent
     * authentication.
     *
     * @return Map of authentication handler names to the authentication errors produced on attempted authentication.
     */
    Map<String, Class<? extends Throwable>> getFailures();

    /**
     * Updates the authentication object with what's passed.
     * Does not override current keys if there are clashes
     *
     * @param authn the authn object
     */
    void update(Authentication authn);

    /**
     * Updates the authentication object with what's passed.
     * Does override current keys if there are clashes.
     * Clears the existing attributes and starts over.
     *
     * @param authn the authn object
     */
    void updateAll(Authentication authn);
}
