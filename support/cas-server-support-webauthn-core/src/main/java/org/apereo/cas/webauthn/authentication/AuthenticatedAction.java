package org.apereo.cas.webauthn.authentication;

import org.apereo.cas.webauthn.util.Either;

import java.util.List;
import java.util.function.Function;

/**
 * This is {@link AuthenticatedAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface AuthenticatedAction<T> extends Function<SuccessfulAuthenticationResult, Either<List<String>, T>> {
}
