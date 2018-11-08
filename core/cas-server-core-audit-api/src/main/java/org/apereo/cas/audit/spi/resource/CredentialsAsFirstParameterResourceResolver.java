package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Converts the Credential object into a String resource identifier.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
public class CredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {

    private static final String SUPPLIED_CREDENTIALS = "Supplied credentials: ";

    /**
     * Turn the arguments into a list.
     *
     * @param args the args
     * @return the string[]
     */
    private static String[] toResources(final Object[] args) {
        val object = args[0];
        if (object instanceof AuthenticationTransaction) {
            val transaction = AuthenticationTransaction.class.cast(object);
            return new String[]{SUPPLIED_CREDENTIALS + transaction.getCredentials()};
        }
        return new String[]{SUPPLIED_CREDENTIALS + CollectionUtils.wrap(object)};
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }
}
