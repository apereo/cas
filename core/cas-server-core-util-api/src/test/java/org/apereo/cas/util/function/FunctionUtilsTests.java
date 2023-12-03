package org.apereo.cas.util.function;

import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FunctionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
@Slf4j
class FunctionUtilsTests {

    @Test
    void verifyDoIf0() throws Throwable {
        val result = new AtomicBoolean();
        FunctionUtils.doIf(true, input -> result.set(true), (Consumer<String>) s -> result.set(false)).accept("input");
        assertTrue(result.get());

        FunctionUtils.doIf(false, input -> result.set(true), (Consumer<String>) s -> result.set(false)).accept("input");
        assertFalse(result.get());
    }

    @Test
    void verifyDoIf1() throws Throwable {
        assertFalse(FunctionUtils.doIf(input -> {
                throw new IllegalArgumentException();
            },
            Suppliers.ofInstance(Boolean.TRUE), Suppliers.ofInstance(Boolean.FALSE), LOGGER).apply(Void.class));
    }

    @Test
    void verifyDoIf2() throws Throwable {
        val trueFunction = new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                throw new IllegalArgumentException();
            }
        };
        assertFalse(FunctionUtils.doIf(true,
            trueFunction, Suppliers.ofInstance(Boolean.FALSE), LOGGER).get());
    }

    @Test
    void verifyDoIf3() throws Throwable {
        assertThrows(IllegalArgumentException.class, () -> FunctionUtils.doIf(input -> {
                throw new IllegalArgumentException();
            },
            t -> null,
            (CheckedFunction<Object, Boolean>) t -> {
                throw new IllegalArgumentException();
            }, LOGGER).apply(Void.class));
    }

    @Test
    void verifyDoIfNull() throws Throwable {
        var supplier = FunctionUtils.doIfNotNull(new Object(), () -> {
            throw new IllegalArgumentException();
        }, Suppliers.ofInstance(Boolean.FALSE), LOGGER);
        assertFalse(supplier.get());

        supplier = FunctionUtils.doIfNotNull(null, () -> Boolean.TRUE, Suppliers.ofInstance(Boolean.FALSE), LOGGER);
        assertFalse(supplier.get());
        supplier = FunctionUtils.doIfNotNull(new Object(), () -> Boolean.TRUE, Suppliers.ofInstance(Boolean.FALSE), LOGGER);
        assertTrue(supplier.get());
    }

    @Test
    void verifyDoIfNull1() throws Throwable {
        assertDoesNotThrow(() -> FunctionUtils.doIfNotNull(Boolean.TRUE, result -> {
            throw new IllegalArgumentException();
        }, LOGGER));
        val supplier = FunctionUtils.doIfNull(null, () -> {
            throw new IllegalArgumentException();
        }, Suppliers.ofInstance(Boolean.FALSE), LOGGER);
        assertFalse(supplier.get());
        assertDoesNotThrow(() -> FunctionUtils.doIfNotNull(null, __ -> {
            throw new IllegalArgumentException();
        }, LOGGER));
    }

    @Test
    void verifyDoIfNull2() throws Throwable {
        assertTrue(FunctionUtils.doIfNull(new Object(), () -> {
            throw new IllegalArgumentException();
        }, Suppliers.ofInstance(Boolean.TRUE), LOGGER).get());
    }

    @Test
    void verifyDoAndHandle() throws Throwable {
        assertThrows(IllegalArgumentException.class,
            () -> FunctionUtils.doAndHandle((CheckedFunction<Object, Boolean>) o -> {
                throw new IllegalArgumentException();
            }, o -> {
                throw new IllegalArgumentException();
            }, LOGGER).apply(Void.class));

        assertFalse(FunctionUtils.doAndHandle((CheckedFunction<Object, Boolean>) o -> {
            throw new IllegalArgumentException();
        }, o -> false, LOGGER).apply(Void.class));
    }

    @Test
    void verifyDoAndHandle2() throws Throwable {
        var supplier = FunctionUtils.doAndHandle(
            () -> {
                throw new IllegalArgumentException();
            }, o -> {
                throw new IllegalArgumentException();
            }, LOGGER);
        assertThrows(IllegalArgumentException.class, supplier::get);
        supplier = FunctionUtils.doAndHandle(
            () -> {
                throw new IllegalArgumentException();
            }, o -> false, LOGGER);
        assertFalse((Boolean) supplier.get());
    }

    @Test
    void verifyDoWithoutThrows() throws Throwable {
        val supplier = FunctionUtils.doWithoutThrows(o -> {
            throw new IllegalArgumentException();
        }, LOGGER);
        assertFalse(supplier);
    }
}
