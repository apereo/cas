package org.apereo.cas.util.spring.boot;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;

/**
 * This is {@link ConditionalOnFeatureEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(CasFeatureEnabledCondition.class)
public @interface ConditionalOnFeatureEnabled {
    /**
     * Feature.
     *
     * @return the feature ref from the catalog
     */
    CasFeatureModule.FeatureCatalog[] feature();

    /**
     * Module identifier.
     *
     * @return the string
     */
    String module() default StringUtils.EMPTY;

    /**
     * Indicate if this feature should be enabled by default
     * if not explicitly enabled.
     *
     * @return true/false
     */
    boolean enabledByDefault() default true;
}



