package org.apereo.cas.util.http;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SimpleHttpClientFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
class SimpleHttpClientFactoryBeanTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = new SimpleHttpClientFactoryBean();
        assertNotNull(input.getObject());
        assertNotNull(input.getObjectType());
        assertTimeout(Duration.ofSeconds(1), input::destroy);
        assertNull(input.getExecutorService());
    }

    @Test
    void verifyExecutorIsForcedToShutDown() throws Throwable {
        val input = new SimpleHttpClientFactoryBean();
        val exec = mock(ExecutorService.class);
        when(exec.awaitTermination(anyLong(), any())).thenReturn(false);
        input.setExecutorService(exec);
        assertDoesNotThrow(input::destroy);
        val ordered = inOrder(exec);
        ordered.verify(exec).shutdown();
        ordered.verify(exec).awaitTermination(anyLong(), any());
        ordered.verify(exec).shutdownNow();
        assertNull(input.getExecutorService());
    }

    @Test
    void verifyExecutorShutdownFailure() throws Throwable {
        val input = new SimpleHttpClientFactoryBean();
        val exec = mock(ExecutorService.class);
        when(exec.awaitTermination(anyLong(), any())).thenThrow(new RuntimeException());
        input.setExecutorService(exec);
        assertDoesNotThrow(input::destroy);
        verify(exec).shutdown();
        verify(exec).shutdownNow();
        assertNull(input.getExecutorService());
    }

    @Test
    void verifyInterruptedExecutorShutdown() throws Throwable {
        val input = new SimpleHttpClientFactoryBean();
        val exec = mock(ExecutorService.class);
        when(exec.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException());
        input.setExecutorService(exec);
        assertDoesNotThrow(input::destroy);
        verify(exec).shutdown();
        verify(exec).shutdownNow();
        assertTrue(Thread.interrupted());
        assertNull(input.getExecutorService());
    }

}
