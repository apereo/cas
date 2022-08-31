package org.apereo.cas.util;

import lombok.val;
import org.springframework.binding.message.MessageContext;
import org.springframework.util.ReflectionUtils;

import java.util.Objects;

import static org.mockito.Mockito.*;

/**
 * This is {@link MockRequestContext}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class MockRequestContext extends org.springframework.webflow.test.MockRequestContext {
    public MockRequestContext(final MessageContext messageContext) throws Exception {
        val field = ReflectionUtils.findField(getClass(), "messageContext");
        Objects.requireNonNull(field).trySetAccessible();
        field.set(this, messageContext);
    }

    public MockRequestContext() throws Exception {
        this(mock(MessageContext.class));
    }
}
