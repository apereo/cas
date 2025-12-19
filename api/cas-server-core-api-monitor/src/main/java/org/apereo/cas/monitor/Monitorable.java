package org.apereo.cas.monitor;

import module java.base;

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
