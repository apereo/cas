package org.jasig.cas.validation;

/**
 * An interface to impose restrictions and requirements on validations (e.g.
 * renew=true).
 *
 * @author William G. Thompson, Jr.
 * @since 3.0.0
 */
public interface ValidationSpecification {

    /**
     * @param assertion The assertion we want to confirm is satisfied by this
     * spec.
     * @return true if it is, false otherwise.
     */
    boolean isSatisfiedBy(Assertion assertion);
}
