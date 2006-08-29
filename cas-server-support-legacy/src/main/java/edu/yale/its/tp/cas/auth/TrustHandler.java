package edu.yale.its.tp.cas.auth;

import javax.servlet.ServletRequest;

/** 
 * CAS 2 interface for  authentication handlers that implement
 * authentication strategies that cannot or should not be 
 * implemented using CAS 2 PasswordHandlers.
 * 
 * TrustHandlers can dynamically determine the authenticated username, something
 * PasswordHandlers cannot do.  While PasswordHandlers can consider the
 * HttpServletRequest itself in determining authentication, a PasswordHandler
 * implementation that doesn't use the password at all would probably be better
 * implemented as a TrustHandler.
 * 
 * When writing authentication plugins for CAS 3 is is preferable to write to the
 * new CAS 3 AuthenticationHandler API rather than to this legacy 
 * CAS 2 API.  However, CAS 2 TrustHandlers can continue to be usable in CAS 3 
 * via adaption.
 * 
 * @version $Revision$ $Date$
 * @since CAS 2.0
 * @see PasswordHandler
 * @see org.jasig.cas.adaptors.cas.LegacyTrustHandlerAdaptorAuthenticationHandler
 */
public interface TrustHandler extends AuthHandler {

    /**
     * Implement this method to compute an authenticated user from a
     * ServletRequest.  You can apply arbitary strategies for determining the username.
     * Your implementation of this method might consider a complex form, check
     * information set by the container, examine cookies, consider a presented client
     * certificate, or even base authentication on the apparent IP address of
     * the request source.
     * 
     * @param request ServletRequest to examine for evidence of authentication
     * @return the authenticated username, or null if no one is authenticated
     */
    String getUsername(ServletRequest request);

}