package org.apereo.cas.util;

import module java.base;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.JoinPoint;

/**
 * Utility class to assist with AOP operations.
 *
 * @author Marvin S. Addison
 * @since 3.4
 */
@UtilityClass
public class AopUtils {

    /**
     * Unwraps a join point that may be nested due to layered proxies.
     *
     * @param point Join point to unwrap.
     * @return Innermost join point; if not nested, returns the argument.
     */
    public static JoinPoint unWrapJoinPoint(final JoinPoint point) {
        var naked = point;
        var args = naked.getArgs();
        while (args != null && args.length > 0 && args[0] instanceof JoinPoint) {
            naked = (JoinPoint) args[0];
            args = naked.getArgs();
        }
        return naked;
    }
}
