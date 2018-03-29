package org.apereo.cas.util.junit;

import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Modifier;

/**
 * This is {@link ConditionalIgnoreRule}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConditionalIgnoreRule implements MethodRule {
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        var result = base;
        if (hasConditionalIgnoreAnnotation(method)) {
            final var condition = getIgnoreCondition(target, method);
            if (condition.isSatisfied()) {
                result = new IgnoreStatement(condition);
            }
        }
        return result;
    }

    /**
     * Has conditional ignore annotation boolean.
     *
     * @param method the method
     * @return the boolean
     */
    private static boolean hasConditionalIgnoreAnnotation(final FrameworkMethod method) {
        return method.getAnnotation(ConditionalIgnore.class) != null;
    }

    /**
     * Gets ignore condition.
     *
     * @param target the target
     * @param method the method
     * @return the ignore condition
     */
    private static IgnoreCondition getIgnoreCondition(final Object target, final FrameworkMethod method) {
        final var annotation = method.getAnnotation(ConditionalIgnore.class);
        return new IgnoreConditionCreator(target, annotation).create();
    }

    /**
     * The type Ignore condition creator.
     */
    private static class IgnoreConditionCreator {
        private final Object target;
        private final Class<? extends IgnoreCondition> conditionType;

        /**
         * Instantiates a new Ignore condition creator.
         *
         * @param target     the target
         * @param annotation the annotation
         */
        IgnoreConditionCreator(final Object target, final ConditionalIgnore annotation) {
            this.target = target;
            this.conditionType = annotation.condition();
        }

        /**
         * Create ignore condition.
         *
         * @return the ignore condition
         */
        IgnoreCondition create() {
            checkConditionType();
            try {
                return createCondition();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Create condition ignore condition.
         *
         * @return the ignore condition
         * @throws Exception the exception
         */
        private IgnoreCondition createCondition() throws Exception {
            final IgnoreCondition result;
            if (isConditionTypeStandalone()) {
                result = conditionType.getDeclaredConstructor().newInstance();
            } else {
                result = conditionType.getDeclaredConstructor(target.getClass()).newInstance(target);
            }
            return result;
        }

        /**
         * Check condition type.
         */
        private void checkConditionType() {
            if (!isConditionTypeStandalone() && !isConditionTypeDeclaredInTarget()) {
                final var msg
                    = "Conditional class '%s' is a member class "
                    + "but was not declared inside the test case using it.\n"
                    + "Either make this class a static class, "
                    + "standalone class (by declaring it in it's own file) "
                    + "or move it inside the test case using it";
                throw new IllegalArgumentException(String.format(msg, conditionType.getName()));
            }
        }

        /**
         * Is condition type standalone boolean.
         *
         * @return the boolean
         */
        private boolean isConditionTypeStandalone() {
            return !conditionType.isMemberClass() || Modifier.isStatic(conditionType.getModifiers());
        }

        /**
         * Is condition type declared in target boolean.
         *
         * @return the boolean
         */
        private boolean isConditionTypeDeclaredInTarget() {
            return target.getClass().isAssignableFrom(conditionType.getDeclaringClass());
        }
    }

    /**
     * The type Ignore statement.
     */
    public static class IgnoreStatement extends Statement {
        private final IgnoreCondition condition;

        /**
         * Instantiates a new Ignore statement.
         *
         * @param condition the condition
         */
        IgnoreStatement(final IgnoreCondition condition) {
            this.condition = condition;
        }

        @Override
        public void evaluate() {
            Assume.assumeTrue("Ignored by " + condition.getClass().getSimpleName(), false);
        }
    }

}

