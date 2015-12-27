package org.jasig.cas.audit.spi;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;

import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.inspektr.audit.spi.AuditResourceResolver;

import org.jasig.cas.util.AopUtils;

/**
 * Converts the Credential object into a String resource identifier.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
public final class CredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    
    /**
     * Turn the arguments into a list.
     *
     * @param args the args
     * @return the string[]
     */
    private static String[] toResources(final Object[] args) {
        final Object object = args[0];
        if (object instanceof AuthenticationTransaction) {
            final AuthenticationTransaction transaction = AuthenticationTransaction.class.cast(object);
            return new String[] {"Supplied credentials: " + transaction.getCredentials()};
        }
        return new String[] {"Supplied credentials: " + Arrays.asList((Object[]) object)};
    }
}
