/**
 * The handler package contains the classes used to authenticate a user.  It contains
 * the AuthenticationHandler interface which is used to validate credential.  It also
 * contains the PasswordEncoders which are used by implementations of the AuthenticationHandler
 * to provide conversion from plain text to whatever the password is encoded as in the data
 * store.
 * The package also contains a well-defined exception heirarchy to allow fine-grained error
 * messages to be displayed.
 * Examples of AuthenticationHandlers implementations:
 * <ul>
 * <li>If the credential are a Userid and Password, then it submits them to an
 * external Kerberos, LDAP, or JDBC authority for validation.</li>
 * <li>If the credential are a Certificate, then it verifies the Issuer chain
 * against some list of reliable CAs, checks the date to make sure it hasn't
 * expired, and checks the CRL to make sure it wasn't revoked.</li>
 * <li>If authentication has been done by the Servlet Container or by a Filter, then
 * the Credentials have been extracted from the HttpRequest object. Notably, this
 * will include the REMOTE_USER. Such Credentials are implicitly trusted and self
 * validating, so an AuthenticationHandler recognizing such an object will indicate
 * that it is valid without inspecting its contents.</li>
 * </ul>
 * @since 3.0
 */
package org.apereo.cas.authentication.handler;

