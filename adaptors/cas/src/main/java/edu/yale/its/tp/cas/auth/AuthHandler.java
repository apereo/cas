package edu.yale.its.tp.cas.auth;

/** 
 * Marker interface for CAS 2 authentication handlers. 
 * 
 * In the context of CAS 3 is is preferable to write your custom authentication logic
 * as a new CAS 3 AuthenticationHandler implementation rather than as an
 * implementation of this legacy CAS 2 AuthHandler API.
 * 
 * However, CAS 2 AuthHandler implementations (at least, PasswordHandler and
 * TrustHandler implementations) continue to work in CAS 3 via adaptor.
 *
 * @since CAS 2.0
 * @version $Revision$ $Date$
 */
public interface AuthHandler {
    // no methods as this is a marker interface
}