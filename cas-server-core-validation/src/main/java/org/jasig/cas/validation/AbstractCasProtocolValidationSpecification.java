package org.jasig.cas.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;

import javax.servlet.http.HttpServletRequest;

/**
 * Base validation specification for the CAS protocol. This specification checks
 * for the presence of renew=true and if requested, succeeds only if ticket
 * validation is occurring from a new login.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Scope(value = "prototype")
public abstract class AbstractCasProtocolValidationSpecification implements ValidationSpecification {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    @Qualifier("authenticationContextValidationSpecification")
    private ValidationSpecification authenticationContextValidationSpecification;

    /** Denotes whether we should always authenticate or not. */
    private boolean renew;

    /**
     * Instantiates a new abstract cas protocol validation specification.
     */
    public AbstractCasProtocolValidationSpecification() {
    }

    /**
     * Instantiates a new abstract cas protocol validation specification.
     *
     * @param renew the renew
     */
    public AbstractCasProtocolValidationSpecification(final boolean renew) {
        this.renew = renew;
    }


    /**
     * Method to set the renew requirement.
     *
     * @param renew The renew value we want.
     */
    public final void setRenew(final boolean renew) {
        this.renew = renew;
    }

    public void setAuthenticationContextValidationSpecification(final ValidationSpecification specification) {
        this.authenticationContextValidationSpecification = specification;
    }

    /**
     * Method to determine if we require renew to be true.
     *
     * @return true if renew is required, false otherwise.
     */
    public final boolean isRenew() {
        return this.renew;
    }

    @Override
    public final boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        return isSatisfiedByInternal(assertion) && (!this.renew || assertion.isFromNewLogin())
            && authenticationContextValidationSpecification.isSatisfiedBy(assertion, request);
    }

    /**
     * Template method to allow for additional checks by subclassed methods
     * without needing to call super.isSatisfiedBy(...).
     * @param assertion the assertion
     * @return true, if the subclass implementation is satisfied by the assertion
     */
    protected abstract boolean isSatisfiedByInternal(Assertion assertion);
}
