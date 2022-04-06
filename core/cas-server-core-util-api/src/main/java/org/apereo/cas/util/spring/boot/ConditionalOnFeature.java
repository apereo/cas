package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.support.CasFeatureModule;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnFeature}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Repeatable(ConditionalOnFeatures.class)
@Conditional(CasFeatureEnabledCondition.class)
public @interface ConditionalOnFeature {
    /**
     * Feature.
     *
     * @return the feature ref from the catalog
     */
    CasFeatureModule.FeatureCatalog feature();

    /**
     * Module identifier.
     *
     * @return the string
     */
    String module() default StringUtils.EMPTY;

    /**
     * Indicate if this feature should be enabled by default
     * if not explicitly enabled.
     * @return true/false
     */
    boolean enabledByDefault() default true;
}



