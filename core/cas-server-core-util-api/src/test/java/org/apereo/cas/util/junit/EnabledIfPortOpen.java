package org.apereo.cas.util.junit;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is {@link EnabledIfPortOpen}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@Target({METHOD, TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@ExtendWith(EnabledIfPortOpenCondition.class)
public @interface EnabledIfPortOpen {
    /**
     * Ports to check for listener.
     *
     * @return TCP port to check
     */
    int[] port();
}
