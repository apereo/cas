package org.apereo.cas.trusted.config;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

/**
 * This is {@link ConditionalOnMultifactorTrustedDevicesEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ConditionalOnMultifactorTrustedDevicesEnabled.MultifactorTrustedDevicesEnabledCondition.class)
public @interface ConditionalOnMultifactorTrustedDevicesEnabled {
    /**
     * Prefix.
     *
     * @return the string
     */
    String prefix();

    /**
     * The type Multifactor trusted devices enabled condition.
     */
    class MultifactorTrustedDevicesEnabledCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            val attributes = Objects.requireNonNull(metadata.getAnnotationAttributes(ConditionalOnMultifactorTrustedDevicesEnabled.class.getName()));
            val name = attributes.get("prefix").toString().concat(".trusted-device-enabled");
            val propertyValue = context.getEnvironment().getProperty(name);

            if (StringUtils.isBlank(propertyValue) || BooleanUtils.toBoolean(propertyValue)) {
                return ConditionOutcome.match("Found matching property for " + name);
            }
            return ConditionOutcome.noMatch("Could not find matching property for " + name);
        }

    }
}
