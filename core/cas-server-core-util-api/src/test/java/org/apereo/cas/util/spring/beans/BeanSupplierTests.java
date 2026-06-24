package org.apereo.cas.util.spring.beans;

import module java.base;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.gen.RandomStringGenerator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BeanSupplierTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
class BeanSupplierTests {
    @Test
    void verifyRequireInterface() {
        assertThrows(IllegalArgumentException.class, () -> BeanSupplier.of(String.class).otherwiseProxy().get());
    }

    @Test
    void verifyBeanSupplied() {
        val noOp = BeanSupplier.of(CipherExecutor.class)
            .alwaysMatch()
            .supply(CipherExecutor::noOp)
            .get();
        assertSame(noOp.getClass(), CipherExecutor.noOp().getClass());
    }

    @Test
    void verifyBeanProxiedWithSupplier() {
        val noOp = BeanSupplier.of(CipherExecutor.class)
            .neverMatch()
            .otherwise(CipherExecutor::noOp)
            .get();
        assertSame(noOp.getClass(), CipherExecutor.noOp().getClass());
    }

    @Test
    void verifyBeanSuppliedWithMultipleConditions() {
        val noOp = BeanSupplier.of(CipherExecutor.class)
            .alwaysMatch()
            .supply(CipherExecutor::noOp)
            .get();
        assertSame(noOp.getClass(), CipherExecutor.noOp().getClass());
    }

    @Test
    void verifyBeanProxied() throws Throwable {
        val r1 = BeanSupplier.of(CipherExecutor.class).neverMatch().otherwiseProxy().get();
        assertTrue(BeanSupplier.isProxy(r1));

        val r2 = BeanSupplier.of(DistributedCacheManager.class).neverMatch().otherwiseProxy().get();
        assertTrue(BeanSupplier.isProxy(r2));
        assertDoesNotThrow(r2::clear);
        assertDoesNotThrow(r2::close);

        assertFalse(r2.contains(null));
        assertTrue(r2.find(o -> true).isEmpty());
        assertNotNull(r2.findAll(o -> true));
        assertNull(r2.getName());

        val r3 = BeanSupplier.of(CasRuntimeModuleLoader.class).otherwiseProxy().get();
        assertTrue(BeanSupplier.isProxy(r3));
        assertTrue(r3.load().isEmpty());

        val r4 = BeanSupplier.of(RandomStringGenerator.class).otherwiseProxy().get();
        assertTrue(BeanSupplier.isProxy(r4));
        assertNull(r4.getAlgorithm());
        assertNotNull(r4.getNewStringAsBytes(0));
        assertNull(r4.getNewString(0));

        val r5 = BeanSupplier.of(Runnable.class).otherwiseProxy().get();
        assertTrue(BeanSupplier.isProxy(r5));
        assertDoesNotThrow(r5::run);
    }

    @Test
    void verifyProxyCallbackOnlyRunsOnFallback() {
        val counter = new AtomicInteger();
        val noOp = BeanSupplier.of(CipherExecutor.class)
            .alwaysMatch()
            .supply(CipherExecutor::noOp)
            .otherwiseProxy(_ -> counter.incrementAndGet())
            .get();
        assertSame(noOp.getClass(), CipherExecutor.noOp().getClass());
        assertEquals(0, counter.get());

        val proxy = BeanSupplier.of(CipherExecutor.class)
            .neverMatch()
            .supply(CipherExecutor::noOp)
            .otherwiseProxy(_ -> counter.incrementAndGet())
            .get();
        assertTrue(BeanSupplier.isProxy(proxy));
        assertEquals(1, counter.get());
    }

    @Test
    void verifyProxyDetectionDoesNotCallToString() {
        val counter = new AtomicInteger();
        val proxy = java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{Runnable.class},
            (_, method, _) -> {
                if ("toString".equals(method.getName())) {
                    counter.incrementAndGet();
                    return BeanSupplier.PROXY_BEAN_TOSTRING_PREFIX + Runnable.class.getName();
                }
                return null;
            });
        assertFalse(BeanSupplier.isProxy(proxy));
        assertEquals(0, counter.get());
    }
}
