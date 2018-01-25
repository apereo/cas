package org.apereo.cas.web.support.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This is {@link AuthenticationThrottlingCondition}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AuthenticationThrottlingCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment(), "cas.authn.throttle.failure.");
        final int threshold = resolver.getProperty("threshold", Integer.class, 0);
        final int rangeSeconds = resolver.getProperty("rangeSeconds", Integer.class, 0);

        if (threshold > 0 && rangeSeconds > 0) {
            return ConditionOutcome.match("Both threshold and rangeSeconds are defined to activate authentication throttling");
        }
        return ConditionOutcome.noMatch("Neither threshold or rangeSeconds are defined to activate authentication throttling");
    }
}
