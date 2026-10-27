package org.apereo.cas.web;

import module java.base;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasSessionStore}. It keeps the state a protocol flow collects before it sends the browser
 * to the CAS login flow, such as a pending SAML2 authentication request, so the flow can pick it up again
 * when the browser returns. Implementations decide where that state lives: the container session,
 * the browser, or the ticket registry.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface CasSessionStore {

    /**
     * Get the value stored under the given key.
     *
     * @param request the request
     * @param key     the key
     * @return the value, if any
     */
    Optional<Object> get(HttpServletRequest request, String key);

    /**
     * Store a value under the given key. A {@code null} value removes the key.
     *
     * @param request  the request
     * @param response the response, used by stores that track state with a cookie
     * @param key      the key
     * @param value    the value
     */
    void set(HttpServletRequest request, HttpServletResponse response, String key, @Nullable Object value);

    /**
     * Get an object that represents the current session and that can travel outside the store,
     * such as the browser storage payload of the browser-backed store.
     *
     * @param request the request
     * @return the trackable session, if any
     */
    Optional<Object> getTrackableSession(HttpServletRequest request);

    /**
     * Restore the session of the current request from an object produced by {@link #getTrackableSession(HttpServletRequest)}.
     *
     * @param request          the request
     * @param trackableSession the trackable session
     */
    void buildFromTrackableSession(HttpServletRequest request, Object trackableSession);
}
