package org.apereo.cas.util.spring.boot;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnFeaturesEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(CasFeatureEnabledCondition.class)
public @interface ConditionalOnFeaturesEnabled {
    /**
     * Conditionals grouped together.
     *
     * @return conditionals
     */
    ConditionalOnFeatureEnabled[] value();
}
