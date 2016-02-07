package org.jasig.cas.validation;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationContextTicketValidationException;
import org.jasig.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

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

    private String authenticationContextAttribute;

    private String authenticationContextParameter;

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

    public String getAuthenticationContextAttribute() {
        return authenticationContextAttribute;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
    }

    public String getAuthenticationContextParameter() {
        return authenticationContextParameter;
    }

    public void setAuthenticationContextParameter(final String authenticationContextParameter) {
        this.authenticationContextParameter = authenticationContextParameter;
    }

    /**
     * Method to set the renew requirement.
     *
     * @param renew The renew value we want.
     */
    public final void setRenew(final boolean renew) {
        this.renew = renew;
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
        final boolean valid = isSatisfiedByInternal(assertion) && (!this.renew || assertion.isFromNewLogin());

        if (valid) {
            final String value = request.getParameter(this.authenticationContextParameter);
            if (StringUtils.isBlank(value)) {
                return true;
            }

            final Object ctxAttr = assertion.getPrimaryAuthentication().getAttributes().get(this.authenticationContextAttribute);
            final Collection<Object> contexts = CollectionUtils.convertValueToCollection(ctxAttr);
            logger.debug("Attempting to match requested authentication context {} against {}", value, contexts);

            if  (contexts.stream().filter(ctx -> ctx.toString().equals(value)).count() > 0) {
                return true;
            }
            throw new UnsatisfiedAuthenticationContextTicketValidationException(assertion.getService());
        }
        return false;
    }

    /**
     * Template method to allow for additional checks by subclassed methods
     * without needing to call super.isSatisfiedBy(...).
     * @param assertion the assertion
     * @return true, if the subclass implementation is satisfied by the assertion
     */
    protected abstract boolean isSatisfiedByInternal(Assertion assertion);
}
