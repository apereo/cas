package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.support.CasFeatureModule;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This is {@link CasFeatureModuleEnabledCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class CasFeatureModuleEnabledCondition extends SpringBootCondition {
    /**
     * Gets property name.
     *
     * @param feature the feature
     * @param module  the module
     * @return the property name
     */
    static String getPropertyName(final CasFeatureModule.FeatureCatalog feature, final String module) {
        return CasFeatureModule.class.getSimpleName() + '.' + feature.name() + '.' + module + ".enabled";
    }

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata metadata) {
        val name = metadata.getAnnotationAttributes(ConditionalOnCasFeatureModule.class.getName()).get("feature").toString();
        val feature = CasFeatureModule.FeatureCatalog.valueOf(name);
        val module = metadata.getAnnotationAttributes(ConditionalOnCasFeatureModule.class.getName()).get("module").toString();
        val property = getPropertyName(feature, module);
        val propertyValue = context.getEnvironment().getProperty(property);
        if (StringUtils.equalsIgnoreCase(propertyValue, "false")) {
            return ConditionOutcome.noMatch("CAS feature module " + feature + '-' + module + " is disabled via " + property);
        }
        return ConditionOutcome.match("CAS feature module " + feature + '-' + module + " is enabled via " + property);
    }
}
