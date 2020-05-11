package org.apereo.cas.util.junit;

import org.apereo.cas.util.SocketUtils;

import lombok.val;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

/**
 * This is {@link EnabledIfPortOpenCondition}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
public class EnabledIfPortOpenCondition implements ExecutionCondition {
    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = ConditionEvaluationResult.enabled("@EnabledIfPortOpen is not present");
    private static final String IGNORE_PORT_CHECK = "IGNORE_PORT_CHECK";

    private static ConditionEvaluationResult enableIfOpen(final EnabledIfPortOpen annotation, final AnnotatedElement element) {
        val ports = annotation.port();
        if (ports.length == 0) {
            throw new IllegalArgumentException("At least one port must be defined");
        }
        if (ignorePortCheck()) {
            return ConditionEvaluationResult.enabled(
                    String.format("%s is enabled because %s environment variable is set", element, IGNORE_PORT_CHECK));
        }
        for (val port : ports) {
            if (port > 0 && SocketUtils.isTcpPortAvailable(port)) {
                return ConditionEvaluationResult.disabled(String.format("%s is disabled because %s is not in use", element, port));
            }
        }
        return ConditionEvaluationResult.enabled(
            String.format("%s is enabled because all ports (%s) is open.", element, Arrays.toString(ports)));
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext extensionContext) {
        val element = extensionContext
            .getElement()
            .orElseThrow(IllegalStateException::new);
        return AnnotationSupport.findAnnotation(element, EnabledIfPortOpen.class)
            .map(annotation -> enableIfOpen(annotation, element))
            .orElse(ENABLED_BY_DEFAULT);
    }

    /**
     * The available port check doesn't always work on Windows so this provides a bypass mechanism.
     * @return true if IGNORE_PORT_CHECK environment variable equals true
     */
    private static boolean ignorePortCheck() {
        val ignorePortCheck = System.getenv(IGNORE_PORT_CHECK);
        return "true".equalsIgnoreCase(ignorePortCheck);
    }
}
