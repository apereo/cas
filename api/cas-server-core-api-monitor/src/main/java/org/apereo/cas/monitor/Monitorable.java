package org.apereo.cas.monitor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link Monitorable}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Monitorable {
    /**
     * Type identifier.
     *
     * @return the string
     */
    String type() default "CAS";
}
