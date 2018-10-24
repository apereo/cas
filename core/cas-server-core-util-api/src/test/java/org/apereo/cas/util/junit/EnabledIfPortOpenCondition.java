package org.apereo.cas.util.junit;

import org.apereo.cas.util.SocketUtils;

import lombok.val;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;

/**
 * This is {@link EnabledIfPortOpenCondition}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class EnabledIfPortOpenCondition implements ExecutionCondition {
    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = ConditionEvaluationResult.enabled("@EnabledIfPortOpen is not present");
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext extensionContext) {
        val element = extensionContext
            .getElement()
            .orElseThrow(IllegalStateException::new);
        return AnnotationSupport.findAnnotation(element, EnabledIfPortOpen.class)
            .map(annotation -> enableIfOpen(annotation, element))
            .orElse(ENABLED_BY_DEFAULT);
    }

    private ConditionEvaluationResult enableIfOpen(final EnabledIfPortOpen annotation, final AnnotatedElement element) {
        val port = annotation.port();
        if (port <= 0) {
            throw new IllegalArgumentException("Port must be positive.");
        }
        if (SocketUtils.isTcpPortAvailable(port)) {
            return ConditionEvaluationResult.disabled(String.format("%s is disabled because %s is not in use", element, port));
        }
        return ConditionEvaluationResult.enabled(String.format("%s is enabled because %s is open.", element, port));
    }
}
