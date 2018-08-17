package org.apereo.cas.util.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalIgnore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalIgnore {
    /**
     * Condition class.
     *
     * @return the class
     */
    Class<? extends IgnoreCondition> condition();

    /**
     * Optional port that needs to be busy when running test.
     *
     * @return port number
     */
    int port() default -1;
}
