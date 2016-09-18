/**
 * <p>Authentication validates the Credentials provided during a /login
 * request. In this context, "Credentials" are an opaque object declared
 * with the Credentials marker interface. The AuthenticationManager
 * typically passes the Credentials to a sequence of plug-in elements
 * to see if any of them can recognize and process the concrete implementing
 * type.</p>
 * <p>Successful authentication generates a Principal object wrapped in an
 * Authentication object. All these objects must be serializable, and the
 * Authentication becomes part of the TGT in the ticket cache.</p>
 * <p>Unsucessful authentication must throw an AuthenticationException. The
 * AuthenticationManager may not return null to signal a failure.</p>
 * @since 3.0
 */
package org.apereo.cas.authentication;
