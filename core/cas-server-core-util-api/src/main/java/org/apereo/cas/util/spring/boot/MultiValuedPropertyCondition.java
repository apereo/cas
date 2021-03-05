package org.apereo.cas.util.spring.boot;

import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

/**
 * This is {@link MultiValuedPropertyCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class MultiValuedPropertyCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        val name = metadata.getAnnotationAttributes(ConditionalOnMultiValuedProperty.class.getName()).get("name").toString();
        val values = List.of((String[]) metadata.getAnnotationAttributes(ConditionalOnMultiValuedProperty.class.getName()).get("value"));

        val matched = values.stream().allMatch(value -> {
            val propertyValue = context.getEnvironment().getProperty(name + '.' + value);
            return propertyValue != null;
        });

        if (matched) {
            return ConditionOutcome.match("Found matching property for " + name);
        }
        return ConditionOutcome.noMatch("Could not find matching property for " + name);
    }

}
