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
 * This is {@link ConditionalOnCasFeatureModule}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(ConditionalOnCasFeatureModules.class)
@Conditional(CasFeatureModuleEnabledCondition.class)
public @interface ConditionalOnCasFeatureModule {
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
}



