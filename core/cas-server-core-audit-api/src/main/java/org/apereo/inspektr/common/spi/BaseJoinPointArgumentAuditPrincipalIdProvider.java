package org.apereo.inspektr.common.spi;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link BaseJoinPointArgumentAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseJoinPointArgumentAuditPrincipalIdProvider<T> implements PrincipalResolver {
    private final int argumentPosition;

    private final Class<T> argumentType;

    @Override
    @Nullable
    public String resolveFrom(final JoinPoint auditTarget, final Object returnValue) {
        if (argumentPosition >= 0
            && argumentPosition <= auditTarget.getArgs().length - 1
            && argumentType.isAssignableFrom(auditTarget.getArgs()[argumentPosition].getClass())) {
            return resolveFrom((T) auditTarget.getArgs()[argumentPosition], auditTarget, returnValue);
        }
        return null;
    }

    @Override
    @Nullable
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        if (argumentPosition >= 0
            && argumentPosition > auditTarget.getArgs().length - 1
            && auditTarget.getArgs()[argumentPosition].getClass().equals(argumentType)) {
            return resolveFrom((T) auditTarget.getArgs()[argumentPosition], auditTarget, exception);
        }
        return null;
    }

    protected abstract String resolveFrom(T argument, JoinPoint auditTarget, Object returnValue);

}
