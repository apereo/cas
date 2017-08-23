package org.apereo.cas.validation;

import javax.servlet.http.HttpServletRequest;

/**
 * An interface to impose restrictions and requirements on validations (e.g.
 * renew=true).
 *
 * @author William G. Thompson, Jr.
 * @since 3.0.0
 */
@FunctionalInterface
public interface CasProtocolValidationSpecification {

    /**
     * Is satisfied?
     *
     * @param assertion The assertion we want to confirm is satisfied by this spec.
     * @param request   the request
     * @return true if it is, false otherwise.
     */
    boolean isSatisfiedBy(Assertion assertion, HttpServletRequest request);

    /**
     * Reset.
     */
    default void reset() {}
}
