package org.apereo.cas.util.spring.boot;

import module java.base;
import org.springframework.context.annotation.Conditional;

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
