package org.apereo.cas.util.junit;

import module java.base;
import org.junit.jupiter.api.extension.ExtendWith;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is {@link EnabledIfListeningOnPort}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@Target({METHOD, TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@ExtendWith(EnabledIfPortOpenCondition.class)
public @interface EnabledIfListeningOnPort {
    /**
     * Ports to check for listener.
     *
     * @return TCP port to check
     */
    int[] port();
}
