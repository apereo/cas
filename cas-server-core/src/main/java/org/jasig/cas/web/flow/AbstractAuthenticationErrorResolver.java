/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerError;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.jasig.cas.authentication.MixedPrincipalException;
import org.jasig.cas.authentication.PasswordChangeRequiredException;
import org.jasig.cas.authentication.PrincipalException;
import org.jasig.cas.authentication.UnresolvedPrincipalException;
import org.jasig.cas.authentication.UnsupportedCredentialException;

/**
 * Abstract base class that provides a facility for mapping exceptions from
 * {@link org.jasig.cas.authentication.AuthenticationManager} components in the authentication tier to a type needed
 * by the Web tier.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public abstract class AbstractAuthenticationErrorResolver<T> {

    private static final Map<Class<? extends Throwable>, String> ERROR_MAP =
            new HashMap<Class<? extends Throwable>, String>();

    static {
        ERROR_MAP.put(AccountException.class, "failed");
        ERROR_MAP.put(AccountDisabledException.class, "accountDisabled");
        ERROR_MAP.put(AccountExpiredException.class, "accountExpired");
        ERROR_MAP.put(AccountLockedException.class, "accountLocked");
        ERROR_MAP.put(AccountNotFoundException.class, "accountNotFound");
        ERROR_MAP.put(CredentialException.class, "failed");
        ERROR_MAP.put(CredentialExpiredException.class, "passwordExpired");
        ERROR_MAP.put(CredentialNotFoundException.class, "failed");
        ERROR_MAP.put(FailedLoginException.class, "failed");
        ERROR_MAP.put(LoginException.class, "failed");
        ERROR_MAP.put(InvalidLoginLocationException.class, "badWorkstation");
        ERROR_MAP.put(InvalidLoginTimeException.class, "badHours");
        ERROR_MAP.put(MixedPrincipalException.class, "mixedPrincipal");
        ERROR_MAP.put(PrincipalException.class, "principalError");
        ERROR_MAP.put(PasswordChangeRequiredException.class, "mustChangePassword");
        ERROR_MAP.put(UnresolvedPrincipalException.class, "unresolvedPrincipal");
        ERROR_MAP.put(UnsupportedCredentialException.class, "unsupportedCredential");
    }

    /**
     * Resolves a type from an {@link org.jasig.cas.authentication.AuthenticationException} thrown by an {@link org.jasig.cas.authentication.AuthenticationManager}.
     *
     * @param e Error for which to resolve a type.
     *
     * @return Type that maps to the given error.
     */
    public T resolve(final Throwable e) {
        if (e instanceof AuthenticationException) {
            final Map<Credential, HandlerError> failures = ((AuthenticationException) e).getFailures();
            if (failures != null && failures.size() > 0) {
                return lookupError(failures.values().iterator().next().getError());
            }
            return getDefault();
        } else if (e instanceof PrincipalException) {
            return lookupError(e);
        }
        throw new IllegalStateException("Authentication failed with unexpected error " + e);
    }

    /**
     * Gets the default type for an exception for which there is no explicit mapping.
     *
     * @return Default type for an exception.
     */
    protected abstract T getDefault();

    /**
     * Gets the type corresponding to a code string.
     *
     * @param code Non-null type code.
     *
     * @return Type for code.
     */
    protected abstract T convertErrorCode(final String code);

    private T lookupError(final Throwable error) {
        final String code = ERROR_MAP.get(error.getClass());
        return code != null ? convertErrorCode(code) : getDefault();
    }
}
