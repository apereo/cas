package org.apereo.cas.authentication.exceptions;

import org.apereo.cas.authentication.AuthenticationException;

/**
 * This is {@link UniquePrincipalRequiredException}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class UniquePrincipalRequiredException extends AuthenticationException {
    private static final String CODE = "UNIQUE_PRINCIPAL_REQUIRED";

    private static final long serialVersionUID = 3532358716666809448L;

    @Override
    public String getCode() {
        return CODE;
    }
}
