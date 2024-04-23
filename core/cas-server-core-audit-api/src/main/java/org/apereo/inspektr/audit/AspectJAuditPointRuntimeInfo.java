package org.apereo.inspektr.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import java.io.Serial;

/**
 * Wrapper around AspectJ's JoinPoint containing the runtime execution info for current audit points.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.6
 */
@RequiredArgsConstructor
class AspectJAuditPointRuntimeInfo implements AuditPointRuntimeInfo {

    @Serial
    private static final long serialVersionUID = 6715135612315786307L;

    private final JoinPoint currentJoinPoint;

    @Override
    public String toString() {
        return currentJoinPoint.toLongString();
    }
}
