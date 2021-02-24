package org.apereo.cas.authentication;

import java.io.Serializable;

/**
 * This is {@link AuthenticationResultBuilderFactory}, which is responsible to
 * produce authentication result builder objects via {@link AuthenticationResultBuilder}
 * and a chain of authentication of history tracked by {@link AuthenticationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface AuthenticationResultBuilderFactory extends Serializable {

    /**
     * New authentication result builder.
     *
     * @return the authentication result builder
     */
    AuthenticationResultBuilder newBuilder();
}
