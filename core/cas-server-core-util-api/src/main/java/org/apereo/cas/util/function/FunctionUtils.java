package org.apereo.cas.util.function;

import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.util.LoggingUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
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
        return doIf(condition, trueFunction, __ -> {
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
     * Conditional function.
     *
     * @param <T>           the type parameter
     * @param <R>           the type parameter
     * @param condition     the condition
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the function
     */
    public static <T, R> Function<T, R> doIf(final Predicate<T> condition,
                                             final CheckedFunction<T, R> trueFunction,
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
     * Do if blank.
     *
     * @param <T>          the type parameter
     * @param input        the input
     * @param trueFunction the true function
     */
    public static <T> void doIfBlank(final CharSequence input, final CheckedConsumer<T> trueFunction) {
        if (StringUtils.isBlank(input)) {
            doAndHandle(trueFunction);
        }
    }

    /**
     * Do if not blank.
     *
     * @param <T>           the type parameter
     * @param input         the input
     * @param trueFunction  the true function
     * @param falseFunction the false function
     * @return the t
     */
    public static <T> T doIfNotBlank(final CharSequence input,
                                     final CheckedSupplier<T> trueFunction,
                                     final CheckedSupplier<T> falseFunction) {
        return doAndHandle(() -> StringUtils.isNotBlank(input) ? trueFunction.get() : falseFunction.get());
    }

    /**
     * Do if not blank.
     *
     * @param input        the input
     * @param trueFunction the true function
     */
    public static <T extends CharSequence> void doIfNotBlank(final T input, final CheckedConsumer<T> trueFunction) {
        try {
            if (StringUtils.isNotBlank(input)) {
                trueFunction.accept(input);
            }
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    /**
     * Do if not null supplier.
     *
     * @param <R>          the type parameter
     * @param input        the input
     * @param trueFunction the true function
     * @return the value from the supplier or null
     */
    public static <R> R doIfNotNull(final Object input,
                                    final CheckedSupplier<R> trueFunction) {
        return doIfNotNull(input, trueFunction, () -> null).get();
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
                                              final CheckedSupplier<R> trueFunction,
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
                                       final CheckedConsumer<T> trueFunction) {
        try {
            if (input != null) {
                trueFunction.accept(input);
            }
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    /**
     * Do if not null.
     *
     * @param <T>          the type parameter
     * @param input        the input
     * @param trueFunction the true function
     * @param elseFunction the else function
     */
    public static <T> void doIfNotNull(final T input,
                                       final CheckedConsumer<T> trueFunction,
                                       final CheckedConsumer<T> elseFunction) {
        try {
            if (input != null) {
                trueFunction.accept(input);
            } else {
                elseFunction.accept(null);
            }
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    /**
     * Do if null.
     *
     * @param <T>          the type parameter
     * @param input        the input
     * @param trueFunction the true function
     */
    public static <T> void doIfNull(final T input,
                                    final CheckedConsumer<T> trueFunction) {
        doIfNull(input, trueFunction, t -> {
        });
    }

    /**
     * Do if null.
     *
     * @param <T>           the type parameter
     * @param input         the input
     * @param trueFunction  the true function
     * @param falseFunction the false function
     */
    public static <T> void doIfNull(final T input,
                                    final CheckedConsumer<T> trueFunction,
                                    final CheckedConsumer<T> falseFunction) {
        try {
            if (input == null) {
                trueFunction.accept(null);
            } else {
                falseFunction.accept(input);
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
     * Default function.
     *
     * @param <T>          the type parameter
     * @param <R>          the type parameter
     * @param function     the function
     * @param errorHandler the error handler
     * @return the function
     */
    public static <T, R> Function<T, R> doAndHandle(final CheckedFunction<T, R> function,
                                                    final CheckedFunction<Throwable, R> errorHandler) {
        return t -> {
            try {
                return function.apply(t);
            } catch (final Throwable e) {
                try {
                    LoggingUtils.warn(LOGGER, e);
                    return errorHandler.apply(e);
                } catch (final Throwable ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }
        };
    }

    /**
     * Do and handle checked consumer.
     *
     * @param <R>          the type parameter
     * @param function     the function
     * @param errorHandler the error handler
     * @return the checked consumer
     */
    public static <R> Consumer<R> doAndHandle(final CheckedConsumer<R> function,
                                              final CheckedFunction<Throwable, R> errorHandler) {
        return value -> {
            try {
                function.accept(value);
            } catch (final Throwable e) {
                try {
                    LoggingUtils.warn(LOGGER, e);
                    errorHandler.apply(e);
                } catch (final Throwable ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
        };
    }

    /**
     * Do and handle.
     *
     * @param <R>      the type parameter
     * @param function the function
     * @return the r
     */
    public static <R> R doAndHandle(final CheckedSupplier<R> function) {
        try {
            return function.get();
        } catch (final InvalidTicketException e) {
            LOGGER.debug(e.getMessage(), e);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }

    /**
     * Do and handle.
     *
     * @param <R>      the type parameter
     * @param function the function
     */
    public static <R> void doAndHandle(final CheckedConsumer<R> function) {
        try {
            function.accept(null);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    /**
     * Do and handle supplier.
     *
     * @param <R>          the type parameter
     * @param function     the function
     * @param errorHandler the error handler
     * @return the supplier
     */
    public static <R> Supplier<R> doAndHandle(final CheckedSupplier<R> function, final CheckedFunction<Throwable, R> errorHandler) {
        return () -> {
            try {
                return function.get();
            } catch (final Throwable e) {
                try {
                    LoggingUtils.warn(LOGGER, e);
                    return errorHandler.apply(e);
                } catch (final Throwable ex) {
                    if (ex instanceof final RuntimeException re) {
                        throw re;
                    }
                    throw new IllegalArgumentException(ex);
                }
            }
        };
    }

    /**
     * Do if condition holds.
     *
     * @param <T>          the type parameter
     * @param condition    the condition
     * @param trueFunction the true function
     */
    public static <T> void doWhen(final boolean condition, final Consumer<T> trueFunction) {
        doIf(condition, trueFunction, __ -> {}).accept(null);
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
     * Do unchecked.
     *
     * @param <T>      the type parameter
     * @param consumer the consumer
     * @return the t
     */
    public static <T> T doUnchecked(final CheckedSupplier<T> consumer) {
        return Unchecked.supplier(consumer).get();
    }

    /**
     * Do unchecked.
     *
     * @param consumer the consumer
     * @param params   the params
     */
    public static void doUnchecked(final CheckedConsumer<Object> consumer, final Object... params) {
        Unchecked.consumer(cons -> consumer.accept(params)).accept(null);
    }

    /**
     * Do and retry.
     *
     * @param <T>      the type parameter
     * @param callback the callback
     * @return the t
     * @throws Exception the exception
     */
    public static <T> T doAndRetry(final RetryCallback<T, Exception> callback) throws Exception {
        return doAndRetry(List.of(), callback);
    }

    /**
     * Do and retry with mix attempts.
     *
     * @param <T>             the type parameter
     * @param callback        the callback
     * @param maximumAttempts the maximum attempts
     * @return the t
     * @throws Exception the exception
     */
    public static <T> T doAndRetry(final RetryCallback<T, Exception> callback, final int maximumAttempts) throws Exception {
        return doAndRetry(List.of(), callback, maximumAttempts);
    }

    /**
     * Do and retry.
     *
     * @param <T>      the type parameter
     * @param clazzes  the classified clazzes
     * @param callback the callback
     * @return the t
     * @throws Exception the exception
     */
    public static <T> T doAndRetry(final List<Class<? extends Throwable>> clazzes,
                                   final RetryCallback<T, Exception> callback) throws Exception {
        return doAndRetry(clazzes, callback, SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Do and retry with a max number of attempts.
     *
     * @param <T>             the type parameter
     * @param clazzes         the clazzes
     * @param callback        the callback
     * @param maximumAttempts the maximum attempts
     * @return the t
     * @throws Exception the exception
     */
    public static <T> T doAndRetry(final List<Class<? extends Throwable>> clazzes,
                                   final RetryCallback<T, Exception> callback,
                                   final int maximumAttempts) throws Exception {
        val retryTemplate = new RetryTemplate();

        val classified = new HashMap<Class<? extends Throwable>, Boolean>();
        classified.put(Error.class, Boolean.TRUE);
        classified.put(Throwable.class, Boolean.TRUE);
        clazzes.forEach(clz -> classified.put(clz, Boolean.TRUE));

        val retryPolicy = maximumAttempts > 0
            ? new SimpleRetryPolicy(maximumAttempts, classified, true)
            : new NeverRetryPolicy();
        retryTemplate.setBackOffPolicy(maximumAttempts > 0
            ? new FixedBackOffPolicy()
            : new NoBackOffPolicy());
        
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setThrowLastExceptionOnExhausted(true);
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public boolean open(final RetryContext context, final RetryCallback __) {
                context.setAttribute("retry.maxAttempts", retryPolicy.getMaxAttempts());
                return RetryListener.super.open(context, __);
            }
        });
        return retryTemplate.execute(callback);
    }

    /**
     * Throw if value is blank.
     *
     * @param value the value
     * @return the value
     * @throws Throwable the throwable
     */
    public static String throwIfBlank(final String value) throws Throwable {
        throwIf(StringUtils.isBlank(value), () -> new IllegalArgumentException("Value cannot be empty or blank"));
        return value;
    }

    /**
     * Throw if null.
     *
     * @param <T>     the type parameter
     * @param value   the value
     * @param handler the handler
     * @return the t
     * @throws Throwable the throwable
     */
    public static <T> T throwIfNull(final T value, final CheckedSupplier<Throwable> handler) throws Throwable {
        throwIf(value == null, handler);
        return value;
    }

    /**
     * Throw if.
     *
     * @param condition the condition
     * @param throwable the throwable
     * @throws Throwable the throwable
     */
    public static void throwIf(final boolean condition, final CheckedSupplier<? extends Throwable> throwable) throws Throwable {
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

    /**
     * Do and throw exception.
     *
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param handler  the handler
     * @return the t
     * @throws Exception the exception
     */
    public static <T> T doAndThrow(final CheckedSupplier<T> supplier, final Function<Throwable, ? extends Exception> handler) throws Exception {
        try {
            return supplier.get();
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            throw handler.apply(e);
        }
    }

    /**
     * Do and throw unchecked.
     *
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param handler  the handler
     * @return the t
     */
    public static <T> T doAndThrowUnchecked(final CheckedSupplier<T> supplier,
                                            final Function<Throwable, ? extends RuntimeException> handler) {
        try {
            return supplier.get();
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            throw handler.apply(e);
        }
    }
}
