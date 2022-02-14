package org.apereo.cas.aup;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnAcceptableUsageEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ConditionalOnAcceptableUsageEnabled {
}
