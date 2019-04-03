package org.apereo.cas.validation;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.ServicesManager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
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
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public abstract class AbstractCasProtocolValidationSpecification implements CasProtocolValidationSpecification {
    private final ServicesManager servicesManager;

    /**
     * Denotes whether we should always authenticate or not.
     */
    private boolean renew;

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        LOGGER.trace("Is validation specification set to enforce [{}] protocol behavior? [{}]. Is assertion issued from a new login? [{}]",
            CasProtocolConstants.PARAMETER_RENEW, BooleanUtils.toStringYesNo(this.renew),
            BooleanUtils.toStringYesNo(assertion.isFromNewLogin()));
        var satisfied = isSatisfiedByInternal(assertion);
        if (!satisfied) {
            LOGGER.warn("[{}] is not internally satisfied by the produced assertion", getClass().getSimpleName());
            return false;
        }
        satisfied = !this.renew || assertion.isFromNewLogin();
        if (!satisfied) {
            LOGGER.warn("[{}] is to enforce the [{}] CAS protocol behavior, yet the assertion is not issued from a new login", getClass().getSimpleName(),
                CasProtocolConstants.PARAMETER_RENEW);
            return false;
        }
        LOGGER.trace("Validation specification is satisfied by the produced assertion");
        return true;
    }

    @Override
    public void reset() {
        setRenew(false);
    }

    /**
     * Template method to allow for additional checks by subclassed methods
     * without needing to call super.isSatisfiedBy(...).
     *
     * @param assertion the assertion
     * @return true, if the subclass implementation is satisfied by the assertion
     */
    protected abstract boolean isSatisfiedByInternal(Assertion assertion);
}
