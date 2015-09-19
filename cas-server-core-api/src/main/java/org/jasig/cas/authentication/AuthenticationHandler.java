/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import java.security.GeneralSecurityException;

/**
 * An authentication handler authenticates a single credential. In many cases credentials are authenticated by
 * comparison with data in a system of record such as LDAP directory or database.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface AuthenticationHandler {

    /**
     * Authenticates the given credential. There are three possible outcomes of this process, and implementers
     * MUST adhere to the following contract:
     *
     * <ol>
     *     <li>Success -- return {@link HandlerResult}</li>
     *     <li>Failure -- throw {@link GeneralSecurityException}</li>
     *     <li>Indeterminate -- throw {@link PreventedException}</li>
     * </ol>
     *
     * @param credential The credential to authenticate.
     *
     * @return A result object containing metadata about a successful authentication event that includes at
     * a minimum the name of the handler that authenticated the credential and some credential metadata.
     * The following data is optional:
     * <ul>
     *     <li>{@link org.jasig.cas.authentication.principal.Principal}</li>
     *     <li>Messages issued by the handler about the credential (e.g. impending password expiration warning)</li>
     * </ul>
     *
     * @throws GeneralSecurityException On authentication failures where the root cause is security related,
     * e.g. invalid credential. Implementing classes SHOULD be as specific as possible in communicating the reason for
     * authentication failure. Recommendations for common cases:
     * <ul>
     *     <li>Bad password: {@link javax.security.auth.login.FailedLoginException}</li>
     *     <li>Expired password: {@link javax.security.auth.login.CredentialExpiredException}</li>
     *     <li>User account expired: {@link javax.security.auth.login.AccountExpiredException}</li>
     *     <li>User account locked: {@link javax.security.auth.login.AccountLockedException}</li>
     *     <li>User account not found: {@link javax.security.auth.login.AccountNotFoundException}</li>
     *     <li>Time of authentication not allowed: org.jasig.cas.authentication.InvalidLoginTimeException</li>
     *     <li>Location of authentication not allowed: org.jasig.cas.authentication.InvalidLoginLocationException</li>
     *     <li>Expired X.509 certificate: {@link java.security.cert.CertificateExpiredException}</li>
     * </ul>
     * @throws PreventedException On errors that prevented authentication from occurring. Implementing classes SHOULD
     * take care to populate the cause, where applicable, with the error that prevented authentication.
     */
    HandlerResult authenticate(Credential credential) throws GeneralSecurityException, PreventedException;


    /**
     * Determines whether the handler has the capability to authenticate the given credential. In practical terms,
     * the {@link #authenticate(Credential)} method MUST be capable of processing a given credential if
     * <code>supports</code> returns true on the same credential.
     *
     * @param credential The credential to check.
     *
     * @return True if the handler supports the Credential, false otherwise.
     */
    boolean supports(Credential credential);


    /**
     * Gets a unique name for this authentication handler within the Spring context that contains it.
     * For implementations that allow setting a unique name, deployers MUST take care to ensure that every
     * handler instance has a unique name.
     *
     * @return Unique name within a Spring context.
     */
    String getName();
}
