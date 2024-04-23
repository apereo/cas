package org.apereo.cas.util.spring.boot;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This is {@link CasGraalVMNativeImageDisabledCondition}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasGraalVMNativeImageDisabledCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return CasRuntimeHintsRegistrar.notInNativeImage()
            ? ConditionOutcome.match("GraalVM build and execution environment missing")
            : ConditionOutcome.noMatch("GraalVM build and execution environment found");
    }
}
