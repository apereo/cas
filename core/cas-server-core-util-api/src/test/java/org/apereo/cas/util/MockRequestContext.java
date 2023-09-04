package org.apereo.cas.util;

import lombok.val;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockExternalContext;
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

    public MockHttpServletRequest getHttpServletRequest() {
        return (MockHttpServletRequest) getExternalContext().getNativeRequest();
    }

    public MockHttpServletResponse getHttpServletResponse() {
        return (MockHttpServletResponse) getExternalContext().getNativeResponse();
    }


    public MockRequestContext setParameter(final String name, final String value) {
        getHttpServletRequest().setParameter(name, value);
        putRequestParameter(name, value);
        return this;
    }


    public static MockRequestContext create() throws Exception {
        val requestContext = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val externalContext = new MockExternalContext();
        externalContext.setNativeContext(new MockServletContext());
        externalContext.setNativeRequest(request);
        externalContext.setNativeResponse(response);
        requestContext.setExternalContext(externalContext);
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(externalContext);
        return requestContext;
    }
}
