package org.apereo.cas.authentication;

import com.google.common.base.Predicates;
import org.apereo.cas.authentication.principal.Principal;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Constructs immutable {@link Authentication} objects using the builder pattern.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface AuthenticationBuilder extends Serializable {

    /**
     * Gets the authenticated principal.
     *
     * @return Principal. principal
     */
    Principal getPrincipal();

    /**
     * Add credentials authentication builder.
     *
     * @param credentials the credentials
     * @return the authentication builder
     * @since 4.2.0
     */
    AuthenticationBuilder addCredentials(List<CredentialMetaData> credentials);

    /**
     * Sets the principal returns this instance.
     *
     * @param p Authenticated principal.
     * @return This builder instance.
     */
    AuthenticationBuilder setPrincipal(Principal p);

    /**
     * Adds metadata about a credential presented for authentication.
     *
     * @param credential Credential metadata.
     * @return This builder instance.
     */
    AuthenticationBuilder addCredential(CredentialMetaData credential);

    /**
     * Adds an authentication metadata attribute key-value pair.
     *
     * @param key   Authentication attribute key.
     * @param value Authentication attribute value.
     * @return This builder instance.
     */
    AuthenticationBuilder addAttribute(String key, Object value);

    /**
     * Gets the authentication success map.
     *
     * @return Non -null map of handler names to successful handler authentication results.
     */
    Map<String, HandlerResult> getSuccesses();

    /**
     * Set successes authentication builder.
     *
     * @param successes the successes
     * @return the authentication builder
     * @since 4.2.0
     */
    AuthenticationBuilder setSuccesses(Map<String, HandlerResult> successes);

    /**
     * Adds successes authentication builder.
     *
     * @param successes the successes
     * @return the authentication builder
     * @since 4.2.0
     */
    AuthenticationBuilder addSuccesses(Map<String, HandlerResult> successes);

    /**
     * Sets failures.
     *
     * @param failures the failures
     * @return the failures
     */
    AuthenticationBuilder setFailures(Map<String, Class<? extends Throwable>> failures);

    /**
     * Adds failures.
     *
     * @param failures the failures
     * @return the failures
     * @since 4.2.0
     */
    AuthenticationBuilder addFailures(Map<String, Class<? extends Throwable>> failures);

    /**
     * Adds an authentication success to the map of handler names to successful authentication handler results.
     *
     * @param key   Authentication handler name.
     * @param value Successful authentication handler result produced by handler of given name.
     * @return This builder instance.
     */
    AuthenticationBuilder addSuccess(String key, HandlerResult value);

    /**
     * Sets the authentication date and returns this instance.
     *
     * @param d Authentication date.
     * @return This builder instance.
     */
    AuthenticationBuilder setAuthenticationDate(ZonedDateTime d);

    /**
     * Creates an immutable authentication instance from builder data.
     *
     * @return Immutable authentication.
     */
    Authentication build();

    /**
     * Gets the authentication failure map.
     *
     * @return Non -null authentication failure map.
     */
    Map<String, Class<? extends Throwable>> getFailures();

    /**
     * Adds an authentication failure to the map of handler names to the authentication handler failures.
     *
     * @param key   Authentication handler name.
     * @param value Exception raised on handler failure to authenticate credential.
     * @return This builder instance.
     */
    AuthenticationBuilder addFailure(String key, Class<? extends Throwable> value);

    /**
     * Sets the authentication metadata attributes.
     *
     * @param attributes Non-null map of authentication metadata attributes.
     * @return This builder instance.
     */
    AuthenticationBuilder setAttributes(Map<String, Object> attributes);

    /**
     * Merge attribute.
     *
     * @param key   the key
     * @param value the value
     * @return the authentication builder
     */
    AuthenticationBuilder mergeAttribute(String key, Object value);

    /**
     * Has attribute boolean.
     *
     * @param name  the name
     * @param value the value
     * @return true /false
     */
    boolean hasAttribute(String name, Predicate<Object> value);

    /**
     * Has attribute boolean.
     *
     * @param name the name
     * @return the boolean
     */
    default boolean hasAttribute(final String name) {
        return hasAttribute(name, Predicates.alwaysTrue());
    }
}
