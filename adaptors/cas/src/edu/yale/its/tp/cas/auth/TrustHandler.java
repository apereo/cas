package edu.yale.its.tp.cas.auth;

import javax.servlet.ServletRequest;

/** Interface for server-based authentication handlers. */
public interface TrustHandler extends AuthHandler {

    /**
     * Allows arbitrary logic to compute an authenticated user from a ServletRequest.
     */
    String getUsername(ServletRequest request);

}
