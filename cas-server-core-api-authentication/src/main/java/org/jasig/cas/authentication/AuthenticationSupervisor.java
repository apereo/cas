package org.jasig.cas.authentication;

/**
 * The {@link AuthenticationSupervisor} deals exclusively with authentication concepts
 * e.g. Credentials, Principals, producing valid Authentication objects. It is invoked repeatedly with distinct credential type(s)
 * for interactive multi-staged authn flows that would authenticate at each step as opposed
 * to gather all credentials and send them for authentication in one batch.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AuthenticationSupervisor {

}
