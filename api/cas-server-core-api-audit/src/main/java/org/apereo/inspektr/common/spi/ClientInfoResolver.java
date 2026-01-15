package org.apereo.inspektr.common.spi;

import module java.base;
import org.apereo.inspektr.common.web.ClientInfo;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * Interface for resolving the {@link ClientInfo} object.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@FunctionalInterface
public interface ClientInfoResolver {

    /**
     * Resolve the ClientInfo from the provided arguments and return value.
     *
     * @param joinPoint the point where the join occurred.
     * @param retVal    the return value from the method call.
     * @return the constructed ClientInfo object.  Should never return null!
     */
    ClientInfo resolveFrom(JoinPoint joinPoint, @Nullable Object retVal);
}
