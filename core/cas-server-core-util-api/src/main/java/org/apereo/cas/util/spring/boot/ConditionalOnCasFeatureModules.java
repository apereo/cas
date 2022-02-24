package org.apereo.cas.util.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnCasFeatureModules}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ConditionalOnCasFeatureModules {
    /**
     * Conditionals grouped together.
     *
     * @return conditionals
     */
    ConditionalOnCasFeatureModule[] value();
}
