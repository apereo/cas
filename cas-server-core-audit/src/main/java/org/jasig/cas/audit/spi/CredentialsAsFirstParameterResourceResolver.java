package org.jasig.cas.audit.spi;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;

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
        return new String[] {"supplied credentials: " + Arrays.asList((Object[]) args[0])};
    }
}
