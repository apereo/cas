package edu.yale.its.tp.cas.auth;

import javax.servlet.ServletRequest;

/** Interface for password-based authentication handlers. */
public interface PasswordHandler extends AuthHandler {

    /**
     * Authenticates the given username/password pair, returning true
     * on success and false on failure.
     */
    boolean authenticate(ServletRequest request, String username, String password);

}
