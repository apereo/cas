package org.apereo.cas.util;

import lombok.val;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockRequestControlContext;
import java.util.Objects;
import static org.mockito.Mockito.*;

/**
 * This is {@link MockRequestContext}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class MockRequestContext extends MockRequestControlContext {
    public MockRequestContext(final MessageContext messageContext) throws Exception {
        setMessageContext(messageContext);
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

    public MockRequestContext setParameter(final String name, final String[] value) {
        getHttpServletRequest().setParameter(name, value);
        putRequestParameter(name, value);
        return this;
    }

    public MockRequestContext setActiveFlow(final Flow flow) {
        setFlowExecutionContext(new MockFlowExecutionContext(flow));
        return this;
    }

    public MockRequestContext addGlobalTransition(final String transitionId, final String targetState) {
        val targetResolver = new DefaultTargetStateResolver(targetState);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(transitionId)), targetResolver);
        getRootFlow().getGlobalTransitionSet().add(transition);
        return this;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return Objects.requireNonNull((ConfigurableApplicationContext) getActiveFlow().getApplicationContext());
    }

    public MockRequestContext setApplicationContext(final ApplicationContext applicationContext) {
        Objects.requireNonNull((Flow) getActiveFlow()).setApplicationContext(applicationContext);
        return this;
    }

    public MockRequestContext setContextPath(final String constContextPath) {
        getHttpServletRequest().setContextPath(constContextPath);
        getMockExternalContext().setContextPath(constContextPath);
        return this;
    }

    public MockRequestContext addHeader(final String name, final Object value) {
        getHttpServletRequest().addHeader(name, value);
        return this;
    }

    public MockRequestContext setSessionAttribute(final String name, final String value) {
        Objects.requireNonNull(getHttpServletRequest().getSession(true)).setAttribute(name, value);
        return this;
    }

    public Object getSessionAttribute(final String name) {
        return Objects.requireNonNull(getHttpServletRequest().getSession(true)).getAttribute(name);
    }


    public MockRequestContext setContentType(final String type) {
        getHttpServletRequest().setContentType(type);
        return this;
    }

    public MockRequestContext setMethod(final HttpMethod method) {
        getHttpServletRequest().setMethod(method.name());
        return this;
    }

    public MockRequestContext setRequestCookiesFromResponse() {
        getHttpServletRequest().setCookies(getHttpServletResponse().getCookies());
        return this;
    }


    public static MockRequestContext create() throws Exception {
        val staticContext = new StaticApplicationContext();
        staticContext.refresh();
        return create(staticContext);
    }

    public static MockRequestContext create(final ApplicationContext applicationContext) throws Exception {
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
        if (applicationContext != null) {
            val flow = (Flow) requestContext.getActiveFlow();
            flow.setApplicationContext(applicationContext);
        }
        return requestContext;
    }

    public MockRequestContext setMessageContext(final MessageContext messageContext) throws Exception {
        val field = ReflectionUtils.findField(getClass(), "messageContext");
        Objects.requireNonNull(field).trySetAccessible();
        field.set(this, messageContext);
        return this;
    }

    public MockRequestContext withDefaultMessageContext() throws Exception {
        setMessageContext(new DefaultMessageContext());
        return this;
    }
}
