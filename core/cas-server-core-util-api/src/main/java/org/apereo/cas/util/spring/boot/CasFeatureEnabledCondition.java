package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.features.CasFeatureModule;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Map;

/**
 * This is {@link CasFeatureEnabledCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
public class CasFeatureEnabledCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata metadata) {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnFeatureEnabled.class.getName());
        if (attributes == null) {
            val conditions = (AnnotationAttributes[]) metadata.getAnnotationAttributes(
                ConditionalOnFeaturesEnabled.class.getName()).get("value");
            val builder = new StringBuilder();
            val match = Arrays.stream(conditions).allMatch(annotation -> {
                val feature = (CasFeatureModule.FeatureCatalog) annotation.get("feature");
                val module = annotation.getString("module");
                val enabledByDefault = annotation.getBoolean("enabledByDefault");
                val conditionOutcome = getConditionOutcome(context, feature, module, enabledByDefault);
                builder.append(conditionOutcome.getMessage()).append(' ');
                return conditionOutcome.isMatch();
            });
            return match ? ConditionOutcome.match(builder.toString()) : ConditionOutcome.noMatch(builder.toString());
        }
        return evaluateFeatureCondition(context, attributes);
    }

    private static ConditionOutcome evaluateFeatureCondition(final ConditionContext context,
                                                             final Map<String, Object> attributes) {
        val feature = (CasFeatureModule.FeatureCatalog) attributes.get("feature");
        val module = (String) attributes.get("module");
        val enabledByDefault = (boolean) attributes.get("enabledByDefault");
        return getConditionOutcome(context, feature, module, enabledByDefault);
    }

    private static ConditionOutcome getConditionOutcome(final ConditionContext context,
                                                        final CasFeatureModule.FeatureCatalog feature,
                                                        final String module,
                                                        final boolean enabledByDefault) {
        val property = feature.toProperty(module);
        LOGGER.trace("Checking for feature module capability via [{}]", property);

        if (!context.getEnvironment().containsProperty(property) && !enabledByDefault) {
            val message = "CAS feature " + property + " is disabled by default and must be explicitly enabled.";
            LOGGER.trace(message);
            return ConditionOutcome.noMatch(message);
        }

        val propertyValue = context.getEnvironment().getProperty(property);
        if (StringUtils.equalsIgnoreCase(propertyValue, "false")) {
            val message = "CAS feature " + property + " is set to false.";
            LOGGER.trace(message);
            return ConditionOutcome.noMatch(message);
        }
        val message = "CAS feature " + property + " is set to true.";
        LOGGER.trace(message);
        feature.register(module);
        return ConditionOutcome.match(message);
    }
}
