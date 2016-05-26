package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.validation.Assertion;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspektr's principal resolver that first checks if the return value of the target joinpoint is instance
 * of {@link org.jasig.cas.client.validation.Assertion} and then retrieves the principal from there.
 *
 * If that is not the case, it delegates to the wrapped {@link TicketOrCredentialPrincipalResolver} which might return
 * <code>org.apereo.inspektr.common.spi.PrincipalResolver.UNKNOWN_USER</code> if it's unable to resolve principal id.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.9
 */
public class AssertionAsReturnValuePrincipalResolver implements PrincipalResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionAsReturnValuePrincipalResolver.class);

    private final TicketOrCredentialPrincipalResolver delegate;

    public AssertionAsReturnValuePrincipalResolver(final TicketOrCredentialPrincipalResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Object returnValue) {
        LOGGER.debug("Trying to see if target's return value is instance of [Assertion]...");
        if(returnValue instanceof Assertion) {
            LOGGER.debug("Assertion instance is found in the return value. Resolving principal id from associated Authentication...");
            final Authentication authentication = Assertion.class.cast(returnValue).getPrimaryAuthentication();
            return this.delegate.getPrincipalIdProvider().getPrincipalIdFrom(authentication);
        }
        LOGGER.debug("Resolving principal from the delegate principal resolver: [{}]...", this.delegate);
        final String principalIdResolvedByDelagate = this.delegate.resolveFrom(auditTarget, returnValue);
        if(UNKNOWN_USER.equals(principalIdResolvedByDelagate)) {
            LOGGER.debug("Meaningful principal id could not be resolved by [{}]. Returning [{}]...", this, principalIdResolvedByDelagate);
        }
        return principalIdResolvedByDelagate;
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        return this.delegate.resolveFrom(auditTarget, exception);
    }

    @Override
    public String resolve() {
        return this.resolve();
    }
}
