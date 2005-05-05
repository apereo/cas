package edu.yale.its.tp.cas.auth;

import javax.servlet.ServletRequest;

/**
 * CAS 2 interface supported in CAS 3 through the LegacyPasswordHandlerAdapter
 * technology. 
 */ 
public interface PasswordHandler extends AuthHandler {

    /**
     * Authenticates the given username/password pair, returning true on success
     * and false on failure.
     */
    boolean authenticate(ServletRequest request, String username,
        String password);

}