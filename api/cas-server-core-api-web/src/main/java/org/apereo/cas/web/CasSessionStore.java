package org.apereo.cas.web;

import module java.base;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
     * Get the identifier of the current session.
     *
     * @param request the request
     * @param create  whether a session should be created when there is none, for stores that create sessions on demand
     * @return the session id, if any
     */
    Optional<String> getSessionId(HttpServletRequest request, boolean create);

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
     * Destroy the current session and everything stored in it.
     *
     * @param request  the request
     * @param response the response, used by stores that track state with a cookie
     * @return true if the session could be destroyed
     */
    @CanIgnoreReturnValue
    boolean destroySession(HttpServletRequest request, HttpServletResponse response);

    /**
     * Give the current session a new identifier while keeping its content, to defeat session fixation.
     *
     * @param request the request
     * @return true if the session was renewed, false if the store does not renew sessions
     */
    @CanIgnoreReturnValue
    boolean renewSession(HttpServletRequest request);

    /**
     * Get an object that represents the current session and that can travel outside the store,
     * such as the browser storage payload of the browser-backed store.
     *
     * @param request the request
     * @return the trackable session, if any
     */
    Optional<Object> getTrackableSession(HttpServletRequest request);

    /**
     * Restore a session from an object produced by {@link #getTrackableSession(HttpServletRequest)}.
     * Stores that keep state outside the container restore it into the current request and return themselves;
     * the container-backed store returns a store that operates on the tracked session itself.
     *
     * @param request          the request
     * @param trackableSession the trackable session
     * @return the store that operates on the restored session
     */
    @CanIgnoreReturnValue
    Optional<CasSessionStore> buildFromTrackableSession(HttpServletRequest request, Object trackableSession);
}
