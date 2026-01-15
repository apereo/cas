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
        val exec = mock(ExecutorService.class);
        when(exec.awaitTermination(anyLong(), any())).thenThrow(new RuntimeException());
        input.setExecutorService(exec);
        assertDoesNotThrow(input::destroy);
    }

}
