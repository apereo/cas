package org.apereo.cas.web.flow.executor;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebflowCipherBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Webflow")
public class WebflowCipherBeanTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("webflowCipherExecutor")
    private CipherExecutor webflowCipherExecutor;

    @Test
    public void verifyOperation() {
        val input = new WebflowCipherBean(webflowCipherExecutor);
        assertThrows(IllegalArgumentException.class,
            () -> input.encrypt(mock(InputStream.class), mock(OutputStream.class)));
        assertThrows(IllegalArgumentException.class,
            () -> input.decrypt(mock(InputStream.class), mock(OutputStream.class)));

        val result = input.encrypt("HelloWorld".getBytes(StandardCharsets.UTF_8));
        assertEquals("HelloWorld", new String(input.decrypt(result), StandardCharsets.UTF_8));
    }

}
