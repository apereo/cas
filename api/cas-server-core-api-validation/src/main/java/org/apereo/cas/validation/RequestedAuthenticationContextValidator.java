package org.apereo.cas.validation;

import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Functional interface to provide a method to validate an authentication against a requested context.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@FunctionalInterface
public interface RequestedAuthenticationContextValidator<T> {

    /**
     * Inspect the current authentication to validate the current requested context.
     *
     * @param assertion - the assertion
     * @param request - the request
     * @return - status
     */
    Pair<Boolean, Optional<T>> validateAuthenticationContext(Assertion assertion, HttpServletRequest request);
}
