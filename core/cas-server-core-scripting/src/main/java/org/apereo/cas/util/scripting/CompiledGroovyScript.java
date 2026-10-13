package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * This is {@link CompiledGroovyScript}.
 * <p>
 * A script is stateless when every field it declares, and every static field of the classes
 * compiled with it, is final and holds an immutable value. A stateless script runs on a new
 * instance per execution, so executions run in parallel. Any other script keeps one shared
 * instance whose executions run one at a time, so the state it keeps between executions stays consistent.
 *
 * @author Misagh Moayyed
 * @param type           the compiled script class
 * @param sharedInstance the instance shared by all executions; {@code null} when the script is stateless
 * @param <T>            the script type
 * @since 8.1.0
 */
@Slf4j
public record CompiledGroovyScript<T extends GroovyObject>(Class<? extends T> type, @Nullable T sharedInstance) {

    private static final Set<Class<?>> IMMUTABLE_TYPES = Set.of(String.class, Boolean.class, Character.class,
        Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
        BigInteger.class, BigDecimal.class, Class.class, Pattern.class);

    /**
     * Inspect the compiled classes and decide how the script is executed.
     *
     * @param <T>             the script type
     * @param type            the main class of the script
     * @param compiledClasses all classes compiled from the script source; the script is treated as stateful
     *                        when they do not include the main class
     * @param prototype       an instance of the main class
     * @return the compiled script
     */
    public static <T extends GroovyObject> CompiledGroovyScript<T> of(final Class<? extends T> type,
                                                                      final Class<?>[] compiledClasses,
                                                                      final T prototype) {
        val stateless = !CasRuntimeHintsRegistrar.inNativeImage()
            && Arrays.asList(compiledClasses).contains(type)
            && isStateless(type, compiledClasses, prototype);
        LOGGER.trace("Groovy script [{}] is [{}]", type.getName(), stateless ? "stateless" : "stateful");
        return new CompiledGroovyScript<>(type, stateless ? null : prototype);
    }

    /**
     * Whether executions share one instance and must run one at a time.
     *
     * @return true or false
     */
    public boolean isStateful() {
        return sharedInstance != null;
    }

    /**
     * Create a new instance for one execution.
     *
     * @param failOnError the fail on error
     * @return the instance
     */
    public @Nullable T newInstance(final boolean failOnError) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            val cause = e instanceof InvocationTargetException && e.getCause() != null ? e.getCause() : e;
            if (failOnError) {
                throw cause instanceof final RuntimeException runtimeException
                    ? runtimeException
                    : new RuntimeException(cause);
            }
            LoggingUtils.error(LOGGER, cause);
        }
        return null;
    }

    private static boolean isStateless(final Class<?> type, final Class<?>[] compiledClasses,
                                       final Object prototype) {
        val compiled = Set.copyOf(Arrays.asList(compiledClasses));
        val instanceFieldsAreImmutable = hierarchyOf(type).allMatch(clazz -> Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .allMatch(field -> isImmutable(field, Modifier.isStatic(field.getModifiers()) ? null : prototype, compiled)));
        return instanceFieldsAreImmutable && compiled.stream()
            .flatMap(clazz -> Arrays.stream(clazz.getDeclaredFields()))
            .filter(field -> !field.isSynthetic() && Modifier.isStatic(field.getModifiers()))
            .allMatch(field -> isImmutable(field, null, compiled));
    }

    private static Stream<Class<?>> hierarchyOf(final Class<?> type) {
        return Stream.<Class<?>>iterate(type,
            clazz -> clazz != null && clazz != Script.class && clazz != GroovyObjectSupport.class && clazz != Object.class,
            Class::getSuperclass);
    }

    private static boolean isImmutable(final Field field, @Nullable final Object target, final Set<Class<?>> compiled) {
        if (!Modifier.isFinal(field.getModifiers())) {
            return false;
        }
        try {
            if (!field.trySetAccessible()) {
                return false;
            }
            val value = field.get(target);
            if (value instanceof final Enum<?> constant && compiled.contains(constant.getDeclaringClass())) {
                return Arrays.stream(constant.getDeclaringClass().getDeclaredFields())
                    .filter(enumField -> !enumField.isSynthetic() && !Modifier.isStatic(enumField.getModifiers()))
                    .allMatch(enumField -> isImmutable(enumField, constant, compiled));
            }
            return value == null || IMMUTABLE_TYPES.contains(value.getClass())
                || value instanceof Enum<?> || value instanceof Logger;
        } catch (final Throwable e) {
            LOGGER.trace("Unable to read field [{}]: [{}]", field, e.getMessage());
            return false;
        }
    }
}
