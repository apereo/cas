package org.apereo.cas.util.function;

import com.google.common.base.Suppliers;
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
public class FunctionUtilsTests {

    @Test
    public void verifyDoIf0() {
        val result = new AtomicBoolean();
        FunctionUtils.doIf(true, input -> result.set(true), (Consumer<String>) s -> result.set(false)).accept("input");
        assertTrue(result.get());

        FunctionUtils.doIf(false, input -> result.set(true), (Consumer<String>) s -> result.set(false)).accept("input");
        assertFalse(result.get());
    }

    @Test
    public void verifyDoIf1() {
        assertFalse(FunctionUtils.doIf(input -> {
                throw new IllegalArgumentException();
            },
            Suppliers.ofInstance(Boolean.TRUE), Suppliers.ofInstance(Boolean.FALSE)).apply(Void.class));
    }

    @Test
    public void verifyDoIf2() {
        val trueFunction = new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                throw new IllegalArgumentException();
            }
        };
        assertFalse(FunctionUtils.doIf(true,
            trueFunction, Suppliers.ofInstance(Boolean.FALSE)).get());
    }

    @Test
    public void verifyDoIf3() {
        assertThrows(IllegalArgumentException.class, () -> FunctionUtils.doIf(input -> {
                throw new IllegalArgumentException();
            },
            t -> null,
            (CheckedFunction<Object, Boolean>) t -> {
                throw new IllegalArgumentException();
            }).apply(Void.class));
    }

    @Test
    public void verifyDoIfNull() {
        var supplier = FunctionUtils.doIfNotNull(new Object(), () -> {
            throw new IllegalArgumentException();
        }, Suppliers.ofInstance(Boolean.FALSE));
        assertFalse(supplier.get());

        supplier = FunctionUtils.doIfNotNull(null, Suppliers.ofInstance(Boolean.TRUE), Suppliers.ofInstance(Boolean.FALSE));
        assertFalse(supplier.get());
        supplier = FunctionUtils.doIfNotNull(new Object(), Suppliers.ofInstance(Boolean.TRUE), Suppliers.ofInstance(Boolean.FALSE));
        assertTrue(supplier.get());
    }

    @Test
    public void verifyDoIfNull1() {
        assertDoesNotThrow(() -> FunctionUtils.doIfNotNull(Boolean.TRUE, result -> {
            throw new IllegalArgumentException();
        }));
        val supplier = FunctionUtils.doIfNull(null, () -> {
            throw new IllegalArgumentException();
        }, Suppliers.ofInstance(Boolean.FALSE));
        assertFalse(supplier.get());
    }

    @Test
    public void verifyDoIfNull2() {
        assertTrue(FunctionUtils.doIfNull(new Object(), () -> {
            throw new IllegalArgumentException();
        }, Suppliers.ofInstance(Boolean.TRUE)).get());
    }

    @Test
    public void verifyDoAndHandle() {
        assertThrows(IllegalArgumentException.class,
            () -> FunctionUtils.doAndHandle(o -> {
                throw new IllegalArgumentException();
            }, (CheckedFunction<Throwable, Boolean>) o -> {
                throw new IllegalArgumentException();
            }).apply(Void.class));

        assertFalse(FunctionUtils.doAndHandle(o -> {
            throw new IllegalArgumentException();
        }, (CheckedFunction<Throwable, Boolean>) o -> false).apply(Void.class));
    }

    @Test
    public void verifyDoAndHandle2() {
        var supplier = FunctionUtils.doAndHandle(
            new Supplier<>() {
                @Override
                public Object get() {
                    throw new IllegalArgumentException();
                }
            }, o -> {
                throw new IllegalArgumentException();
            });
        assertThrows(IllegalArgumentException.class, supplier::get);
        supplier = FunctionUtils.doAndHandle(
            new Supplier<>() {
                @Override
                public Object get() {
                    throw new IllegalArgumentException();
                }
            }, o -> false);
        assertFalse((Boolean) supplier.get());
    }

    @Test
    public void verifyDoWithoutThrows() {
        val supplier = FunctionUtils.doWithoutThrows(o -> {
            throw new IllegalArgumentException();
        });
        assertFalse(supplier);
    }
}
