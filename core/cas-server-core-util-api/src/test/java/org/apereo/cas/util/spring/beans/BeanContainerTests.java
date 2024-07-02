package org.apereo.cas.util.spring.beans;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.DisposableBean;

import java.io.Closeable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BeanContainerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
class BeanContainerTests {
    @Test
    void verifyOperation() throws Throwable {
        var results = BeanContainer.of(List.of("one")).and("two").toList();
        assertEquals(2, results.size());

        results = BeanContainer.of("one").and("two").toList();
        assertEquals(2, results.size());

        var set = BeanContainer.of("one").and("one").toSet();
        assertEquals(1, set.size());

        set = BeanContainer.of(Set.of("hello", "world")).toSet();
        assertEquals(2, set.size());

        assertEquals(1, BeanContainer.of("one").size());

        results = BeanContainer.<String>empty().allOf(List.of("one", "two")).toList();
        assertEquals(2, results.size());

        val ds = mock(DisposableBean.class);
        val closable = mock(Closeable.class);
        val destroyed = new AtomicInteger(0);
        val answer = new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                destroyed.incrementAndGet();
                return null;
            }
        };

        doAnswer(answer).when(ds).destroy();
        doAnswer(answer).when(closable).close();
        val sources = BeanContainer.of(ds, closable);
        sources.destroy();
        assertEquals(2, destroyed.get());
    }
}
