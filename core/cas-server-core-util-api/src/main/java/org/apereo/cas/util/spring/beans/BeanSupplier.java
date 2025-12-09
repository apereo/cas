package org.apereo.cas.util.spring.beans;

import org.apereo.cas.util.ResourceUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.jspecify.annotations.NonNull;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is {@link BeanSupplier}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface BeanSupplier<T> extends Supplier<T> {

    /**
     * ToString() prefix.
     */
    String PROXY_BEAN_TOSTRING_PREFIX = "ProxyBean-";

    /**
     * Create bean supplier for type.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the bean supplier
     */
    static <T> BeanSupplier<T> of(final Class<T> clazz) {
        return new DefaultBeanSupplier<>(clazz);
    }

    /**
     * Is proxy class?
     *
     * @param result the result
     * @return true/false
     */
    static boolean isProxy(final Object result) {
        return result != null && Proxy.isProxyClass(result.getClass())
               && result.toString().startsWith(PROXY_BEAN_TOSTRING_PREFIX);
    }

    /**
     * Is not proxy class?
     *
     * @param result the result
     * @return true/false
     */
    static boolean isNotProxy(final Object result) {
        return result != null && !isProxy(result);
    }

    /**
     * If not proxy, then do.
     *
     * @param object   the object
     * @param consumer the consumer
     */
    static <T> void ifNotProxy(final T object, final Consumer<T> consumer) {
        if (isNotProxy(object)) {
            consumer.accept(object);
        }
    }

    @Override
    T get();

    /**
     * Always true condition.
     *
     * @return the bean supplier
     */
    default BeanSupplier<T> alwaysMatch() {
        return when(true);
    }

    /**
     * Always false condition.
     *
     * @return the bean supplier
     */
    default BeanSupplier<T> neverMatch() {
        return when(false);
    }

    /**
     * Specify condition for bean supplier to create beans.
     *
     * @param conditionSupplier the condition supplier
     * @return the bean supplier
     */
    BeanSupplier<T> when(Supplier<Boolean> conditionSupplier);

    /**
     * Specify condition for bean supplier to create beans.
     *
     * @param rawValue the raw value
     * @return the bean supplier
     */
    default BeanSupplier<T> when(final boolean rawValue) {
        return when(() -> rawValue);
    }

    /**
     * Add condition to ensure given resource exist.
     *
     * @param resource the resource
     * @return the bean supplier
     */
    default BeanSupplier<T> ifExists(final String resource) {
        return when(() -> ResourceUtils.doesResourceExist(resource));
    }

    /**
     * Combine conditions together.
     *
     * @param conditionSupplier the condition supplier
     * @return the bean supplier
     */
    default BeanSupplier<T> and(final Supplier<Boolean> conditionSupplier) {
        return when(conditionSupplier);
    }

    /**
     * Create the bean via a given supplier when condition passes.
     *
     * @param beanSupplier the bean supplier
     * @return the bean supplier
     */
    BeanSupplier<T> supply(Supplier<T> beanSupplier);

    /**
     * Provide bean supplier.
     *
     * @param beanSupplier the bean supplier
     * @return the bean supplier
     */
    BeanSupplier<T> supplyUnchecked(CheckedSupplier<T> beanSupplier);

    /**
     * Create the proxy bean via a given supplier when the condition fails.
     *
     * @param beanSupplier the bean supplier
     * @return the bean supplier
     */
    BeanSupplier<T> otherwise(Supplier<T> beanSupplier);

    /**
     * Create the proxy bean via a given supplier when the condition fails.
     *
     * @return the bean supplier
     */
    default BeanSupplier<T> otherwiseProxy() {
        return otherwiseProxy(null);
    }

    /**
     * Otherwise proxy bean supplier.
     *
     * @param beforeCallback the callback to execute before proxy is created
     * @return the bean supplier
     */
    BeanSupplier<T> otherwiseProxy(Consumer<T> beforeCallback);
    
    /**
     * Create a null bean.
     *
     * @return the bean supplier
     */
    BeanSupplier<T> otherwiseNull();

    @RequiredArgsConstructor
    class DefaultBeanSupplier<T> implements BeanSupplier<T> {
        @NonNull
        private final Class<T> clazz;

        private final List<Supplier<Boolean>> conditionSuppliers = new ArrayList<>();

        private Supplier<T> beanSupplier;

        private Supplier<T> otherwiseSupplier;

        @Override
        public T get() {
            if (!conditionSuppliers.isEmpty() && conditionSuppliers.stream().allMatch(Supplier::get)) {
                return beanSupplier.get();
            }
            return otherwiseSupplier.get();
        }

        @Override
        @CanIgnoreReturnValue
        public BeanSupplier<T> when(final Supplier<Boolean> conditionSupplier) {
            this.conditionSuppliers.add(conditionSupplier);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public BeanSupplier<T> supply(final Supplier<T> beanSupplier) {
            this.beanSupplier = beanSupplier;
            return this;
        }

        @Override
        public BeanSupplier<T> supplyUnchecked(final CheckedSupplier<T> beanSupplier) {
            this.beanSupplier = Unchecked.supplier(beanSupplier);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public BeanSupplier<T> otherwise(final Supplier<T> beanSupplier) {
            this.otherwiseSupplier = beanSupplier;
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public BeanSupplier<T> otherwiseProxy(final Consumer<T> beforeCallback) {
            if (beforeCallback != null) {
                beforeCallback.accept(null);
            }
            return otherwise(new ProxiedBeanSupplier<>(this.clazz));
        }

        @Override
        @CanIgnoreReturnValue
        public BeanSupplier<T> otherwiseNull() {
            return otherwise(new NullBeanSupplier<>());
        }
    }

    class NullBeanSupplier<T> implements Supplier<T> {
        @Override
        public T get() {
            return null;
        }
    }

    @RequiredArgsConstructor
    class ProxiedBeanSupplier<T> implements Supplier<T> {
        private static final Map<Class, Object> TYPES_AND_VALUES;

        private static final Map<String, Object> PROXIES = new ConcurrentHashMap<>();

        static {
            TYPES_AND_VALUES = new HashMap<>();
            TYPES_AND_VALUES.put(Object[].class, ArrayUtils.EMPTY_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(Class[].class, ArrayUtils.EMPTY_CLASS_ARRAY);
            TYPES_AND_VALUES.put(String[].class, ArrayUtils.EMPTY_STRING_ARRAY);
            TYPES_AND_VALUES.put(Integer[].class, ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(int[].class, ArrayUtils.EMPTY_INT_ARRAY);
            TYPES_AND_VALUES.put(Byte[].class, ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(byte[].class, ArrayUtils.EMPTY_BYTE_ARRAY);
            TYPES_AND_VALUES.put(Boolean[].class, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(boolean[].class, ArrayUtils.EMPTY_BOOLEAN_ARRAY);
            TYPES_AND_VALUES.put(Double[].class, ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(double[].class, ArrayUtils.EMPTY_DOUBLE_ARRAY);
            TYPES_AND_VALUES.put(Long[].class, ArrayUtils.EMPTY_LONG_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(long[].class, ArrayUtils.EMPTY_LONG_ARRAY);
            TYPES_AND_VALUES.put(Float[].class, ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY);
            TYPES_AND_VALUES.put(float[].class, ArrayUtils.EMPTY_FLOAT_ARRAY);

            TYPES_AND_VALUES.put(Optional.class, Optional.empty());

            TYPES_AND_VALUES.put(double.class, 0.0D);
            TYPES_AND_VALUES.put(Double.class, 0.0D);
            TYPES_AND_VALUES.put(long.class, 0L);
            TYPES_AND_VALUES.put(Long.class, 0L);
            TYPES_AND_VALUES.put(int.class, 0);
            TYPES_AND_VALUES.put(Integer.class, 0);
            TYPES_AND_VALUES.put(float.class, 0);
            TYPES_AND_VALUES.put(Float.class, 0);
            TYPES_AND_VALUES.put(boolean.class, false);
            TYPES_AND_VALUES.put(Boolean.class, Boolean.FALSE);

            TYPES_AND_VALUES.put(List.class, new ArrayList<>());
            TYPES_AND_VALUES.put(Set.class, new HashSet<>());
            TYPES_AND_VALUES.put(Map.class, new HashMap<>());
            TYPES_AND_VALUES.put(Collection.class, new ArrayList<>());
        }

        private final Class<T> clazz;

        @Override
        public T get() {
            if (!clazz.isInterface()) {
                throw new IllegalArgumentException("Cannot create bean supplier proxy for non-interface type " + clazz.getSimpleName());
            }
            return (T) PROXIES.computeIfAbsent(clazz.getName(),
                s -> Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{clazz},
                    (proxy, method, args) -> {
                        if ("toString".equals(method.getName())) {
                            return PROXY_BEAN_TOSTRING_PREFIX + clazz.getName();
                        }
                        val returnType = method.getReturnType();
                        return TYPES_AND_VALUES.getOrDefault(returnType, null);
                    }));
        }
    }


}
