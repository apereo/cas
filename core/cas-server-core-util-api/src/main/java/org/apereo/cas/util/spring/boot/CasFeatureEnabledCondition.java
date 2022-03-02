package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.support.CasFeatureModule;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This is {@link CasFeatureEnabledCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
public class CasFeatureEnabledCondition extends SpringBootCondition {
    /**
     * Gets property name.
     *
     * @param feature the feature
     * @param module  the module
     * @return the property name
     */
    public static String getPropertyName(final CasFeatureModule.FeatureCatalog feature, final String module) {
        var name = CasFeatureModule.class.getSimpleName() + '.' + feature.name();
        if (StringUtils.isNotBlank(module)) {
            name += '.' + module;
        }
        name += ".enabled";
        return name;
    }

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata metadata) {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnFeature.class.getName());
        val name = attributes.get("feature").toString();
        val module = attributes.get("module").toString();
        
        val feature = CasFeatureModule.FeatureCatalog.valueOf(name);
        val property = getPropertyName(feature, module);
        LOGGER.trace("Checking for feature module capability via [{}]", property);
        val propertyValue = context.getEnvironment().getProperty(property);
        if (StringUtils.equalsIgnoreCase(propertyValue, "false")) {
            val message = "CAS feature " + property  + " is disabled.";
            LOGGER.trace(message);
            return ConditionOutcome.noMatch(message);
        }
        val message = "CAS feature " + property + " is enabled.";
        LOGGER.debug(message);
        return ConditionOutcome.match(message);
    }
}
