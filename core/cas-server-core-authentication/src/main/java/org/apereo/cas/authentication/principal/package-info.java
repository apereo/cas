/**
 * <p>Credentials is a marker interface for an opaque object that may be recognized by
 * Handlers and Resolvers. Credentials may be a UserId/Password, Certificate,
 * RemoteUser, IP address, etc.</p>
 * <p>When the authentication manager is
 * used, that bean is configured with a list of {@link org.apereo.cas.authentication.AuthenticationHandler} that
 * validate Credentials and {@link org.apereo.cas.authentication.principal.PrincipalResolver} that turn
 * {@link org.apereo.cas.authentication.Credential} objects into
 * into {@link org.apereo.cas.authentication.principal.Principal} objects.</p>
 * <p>The Authentication Handler validates credentials and in certain cases is able extract
 * information. The extraction use case is clearer when credentials are certificates.
 * A certificate is valid if you trust the CA, if it hasn't expired, and if it isn't revoked.
 * You can decide all this, and still not have the foggiest idea what ID to give to the person (if
 * it is a person) represented by the Certificate.</p>
 * <p>The {@link org.apereo.cas.authentication.principal.PrincipalResolver}
 * looks into previously validated credentials to construct a Principal object containing an ID (and in more
 * complex cases some attributes). The {@link org.apereo.cas.authentication.principal.resolvers.ProxyingPrincipalResolver} takes
 * credentials and creates a SimplePrincipal containing the Userid.</p>
 * @since 3.0
 */
package org.apereo.cas.authentication.principal;

