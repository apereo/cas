package org.apereo.cas.config;

import org.apereo.cas.aup.ConditionalOnAcceptableUsageEnabled;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnAcceptableUsageRedisEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Conditional(ConditionalOnAcceptableUsageRedisEnabled.AcceptableUsageRedisCondition.class)
public @interface ConditionalOnAcceptableUsageRedisEnabled {
    class AcceptableUsageRedisCondition extends AllNestedConditions {
        AcceptableUsageRedisCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnAcceptableUsageEnabled
        static class OnAcceptableUsagePolicyEnabled {
        }

        @ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
        static class OnAcceptableUsagePolicyRedisEnabled {
        }

    }
}

