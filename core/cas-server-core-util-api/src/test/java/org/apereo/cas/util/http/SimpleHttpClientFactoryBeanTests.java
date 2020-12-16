package org.apereo.cas.util.http;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SimpleHttpClientFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class SimpleHttpClientFactoryBeanTests {

    @Test
    public void verifyOperation() throws Exception {
        val input = new SimpleHttpClientFactoryBean();
        assertNotNull(input.getObject());
        assertNotNull(input.getObjectType());
        val exec = mock(ExecutorService.class);
        when(exec.awaitTermination(anyLong(), any())).thenThrow(new RuntimeException());
        input.setExecutorService(exec);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                input.destroy();
            }
        });
    }

}
