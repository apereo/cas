package edu.yale.its.tp.cas.auth;

import javax.servlet.ServletRequest;

/**
 * CAS 2 interface for implementations of particular strategies for authenticating
 * asserted usernames and passwords.
 * 
 * This CAS 2 interface supported in CAS 3 through the 
 * LegacyPasswordHandlerAdaptorAuthenticationHandler adaptor. 
 * 
 * Writing to this interface when providing custom authentication plugins for
 * CAS 3 is discouraged. New PasswordHandler implementations should probably
 * instead be written to the new CAS 3 APIs (as AuthenticationHandlers).  
 * However, CAS 2 PasswordHandlers remain quite usable in CAS 3 by means of 
 * the referenced adaptor.
 * 
 * @since CAS 2.0
 * @version $Revision$ $Date$
 */ 
public interface PasswordHandler extends AuthHandler {

    /**
     * Authenticates the given username/password pair, returning true on success
     * and false on failure.  True authenticates the presented username.  If you
     * would like to be able to authenticate a request as a username other than that
     * presented, e.g. to alias or standardize character case or remove extraneous
     * whitespace or the like, implementing TrustHandler provides more flexibility in 
     * dynamically determining the authenticated username.
     * 
     * This API provides access to the raw ServletRequest to allow consideration of
     * characteristics of the request other than just the presented username and 
     * password, e.g. the IP address fom which the request appears to be originating,
     * for such purposes as throttling repeated failed authentication requests from
     * apparent bots.  If an implementation is not using the password at all or is
     * doing very much beyond username/password validation, then implementing
     * {@link TrustHandler} rather than PasswordHandler may be preferable.
     * 
     * This class is deprecated to draw attention to the opportunities provided by
     * writing your username and password authentication strategy directly to the
     * CAS 3 authentication APIs.  However, CAS 2 PasswordHandlers work well in
     * CAS 3 via an adaptor.  Though this API is deprecated, your code written to
     * this API is still quite usable in CAS 3.
     * 
     * @param request ServletRequest from which the username and password were extracted
     * @param username as which the request is attempting to authenticate
     * @param password the password paremeter presented on the request
     * @return true to authenticate the request as the username, false to fail the authentication
     * @since CAS 2.0
     * @see TrustHandler
     */
    boolean authenticate(ServletRequest request, String username,
        String password);

}