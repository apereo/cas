package edu.yale.its.tp.cas.auth;

/** 
 * Marker interface for CAS 2 authentication handlers. 
 * 
 * Deprecated in the context of CAS 3 as CAS 3 provides new authentication APIs.
 * However, CAS 2 AuthHandler implementations continue to work (via adaptor) in
 * CAS 3.
 *
 * @since CAS 2.0
 * @version $Revision$ $Date$
 * @deprecated Recommendation is to use the CAS 3 APIs.  See type comments. 
 */
public interface AuthHandler {
    // no methods as this is a marker interface
}