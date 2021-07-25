package org.apereo.cas.util.function;

import org.apereo.cas.util.LoggingUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This is {@link FunctionUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@UtilityClass
public class FunctionUtils {

    /**
     * Do if function.
     *
     * @param <T>           the type parameter
     * @param <R>           the type parameter
     * @param condition     the condition
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the function
     */
    public static <T, R> Function<T, R> doIf(final Predicate<Object> condition, final Supplier<R> trueFunction,
                                             final Supplier<R> falseFunction) {
        return t -> {
            try {
                if (condition.test(t)) {
                    return trueFunction.get();
                }
                return falseFunction.get();
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                return falseFunction.get();
            }
        };
    }

    /**
     * Do if consumer.
     *
     * @param <T>           the type parameter
     * @param condition     the condition
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the consumer
     */
    public static <T> Consumer<T> doIf(final boolean condition, final Consumer<T> trueFunction,
                                       final Consumer<T> falseFunction) {
        return account -> {
            if (condition) {
                trueFunction.accept(account);
            } else {
                falseFunction.accept(account);
            }
        };
    }

    /**
     * Do if condition holds.
     *
     * @param <T>          the type parameter
     * @param condition    the condition
     * @param trueFunction the true function
     * @return the consumer
     */
    public static <T> Consumer<T> doIf(final boolean condition, final Consumer<T> trueFunction) {
        return doIf(condition, trueFunction, t -> {
        });
    }

    /**
     * Do if function.
     *
     * @param <R>           the type parameter
     * @param condition     the condition
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the function
     */
    public static <R> Supplier<R> doIf(final boolean condition, final Supplier<R> trueFunction,
                                       final Supplier<R> falseFunction) {
        return () -> {
            try {
                if (condition) {
                    return trueFunction.get();
                }
                return falseFunction.get();
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                return falseFunction.get();
            }
        };
    }

    /**
     * Conditional function function.
     *
     * @param <T>           the type parameter
     * @param <R>           the type parameter
     * @param condition     the condition
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the function
     */
    public static <T, R> Function<T, R> doIf(final Predicate<T> condition, final CheckedFunction<T, R> trueFunction,
                                             final CheckedFunction<T, R> falseFunction) {
        return t -> {
            try {
                if (condition.test(t)) {
                    return trueFunction.apply(t);
                }
                return falseFunction.apply(t);
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                try {
                    return falseFunction.apply(t);
                } catch (final Throwable ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }
        };
    }

    /**
     * Supply if not null supplier.
     *
     * @param <R>           the type parameter
     * @param input         the input
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the supplier
     */
    public static <R> Supplier<R> doIfNotNull(final Object input,
                                              final Supplier<R> trueFunction,
                                              final Supplier<R> falseFunction) {
        return () -> {
            try {
                if (input != null) {
                    return trueFunction.get();
                }
                return falseFunction.get();
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                return falseFunction.get();
            }
        };
    }

    /**
     * Do if not null.
     *
     * @param <T>          the type parameter
     * @param input        the input
     * @param trueFunction the true function
     */
    public static <T> void doIfNotNull(final T input,
                                       final Consumer<T> trueFunction) {
        try {
            if (input != null) {
                trueFunction.accept(input);
            }
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    /**
     * Supply if null supplier.
     *
     * @param <R>           the type parameter
     * @param input         the input
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the supplier
     */
    public static <R> Supplier<R> doIfNull(final Object input,
                                           final Supplier<R> trueFunction,
                                           final Supplier<R> falseFunction) {
        return () -> {
            try {
                if (input == null) {
                    return trueFunction.get();
                }
                return falseFunction.get();
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                return falseFunction.get();
            }
        };
    }

    /**
     * Default function function.
     *
     * @param <T>          the type parameter
     * @param <R>          the type parameter
     * @param function     the function
     * @param errorHandler the error handler
     * @return the function
     */
    public static <T, R> Function<T, R> doAndHandle(final CheckedFunction<T, R> function, final CheckedFunction<Throwable, R> errorHandler) {
        return t -> {
            try {
                return function.apply(t);
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                try {
                    return errorHandler.apply(e);
                } catch (final Throwable ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }
        };
    }

    /**
     * Do and handle supplier.
     *
     * @param <R>          the type parameter
     * @param function     the function
     * @param errorHandler the error handler
     * @return the supplier
     */
    public static <R> Supplier<R> doAndHandle(final Supplier<R> function, final CheckedFunction<Throwable, R> errorHandler) {
        return () -> {
            try {
                return function.get();
            } catch (final Throwable e) {
                LoggingUtils.warn(LOGGER, e);
                try {
                    return errorHandler.apply(e);
                } catch (final Throwable ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }
        };
    }

    /**
     * Do without throws and return status.
     *
     * @param func   the func
     * @param params the params
     * @return true /false
     */
    public static boolean doWithoutThrows(final Consumer<Object> func, final Object... params) {
        try {
            func.accept(params);
            return true;
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return false;
        }
    }

    /**
     * Do and ignore.
     *
     * @param consumer the consumer
     * @param params   the params
     */
    public static void doAndIgnore(final CheckedConsumer<Object> consumer, final Object... params) {
        Unchecked.consumer(s -> consumer.accept(params)).accept(null);
    }

    /**
     * Do and retry.
     *
     * @param <T>      the type parameter
     * @param callback the callback
     * @return the t
     */
    @SneakyThrows
    public static <T> T doAndRetry(final RetryCallback<T, Exception> callback) {
        return doAndRetry(List.of(), callback);
    }

    /**
     * Do and retry.
     *
     * @param <T>      the type parameter
     * @param clazzes  the classified clazzes
     * @param callback the callback
     * @return the t
     */
    @SneakyThrows
    public static <T> T doAndRetry(final List<Class<? extends Throwable>> clazzes,
                                   final RetryCallback<T, Exception> callback) {
        val retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());

        val classified = new HashMap<Class<? extends Throwable>, Boolean>();
        classified.put(Error.class, Boolean.TRUE);
        classified.put(Throwable.class, Boolean.TRUE);
        clazzes.forEach(clz -> classified.put(clz, Boolean.TRUE));

        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS, classified, true));
        retryTemplate.setThrowLastExceptionOnExhausted(true);
        return retryTemplate.execute(callback);
    }

    /**
     * Throw if value is blank.
     *
     * @param value the value
     * @return the value
     */
    public static String throwIfBlank(final String value) {
        throwIf(StringUtils.isBlank(value), () -> new IllegalArgumentException("Value cannot be empty or blank"));
        return value;
    }

    /**
     * Throw if.
     *
     * @param condition the condition
     * @param throwable the throwable
     */
    public static void throwIf(final boolean condition,
                               final Supplier<? extends RuntimeException> throwable) {
        if (condition) {
            throw throwable.get();
        }
    }

    /**
     * Do and return.
     *
     * @param <T>       the type parameter
     * @param condition the condition
     * @param trueTask  the true task
     * @param falseTask the false task
     * @return the ticket
     */
    public static <T> T doAndReturn(final boolean condition, final Supplier<T> trueTask,
                                    final Supplier<T> falseTask) {
        return condition ? trueTask.get() : falseTask.get();
    }
}
