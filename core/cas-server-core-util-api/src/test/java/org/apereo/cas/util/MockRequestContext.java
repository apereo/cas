package org.apereo.cas.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpHeaders;
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
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestControlContext;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
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
    
    public MockRequestContext setContent(final Object value) {
        getHttpServletRequest().setContent(value.toString().getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public MockRequestContext setPreferredLocales(final Locale... values) {
        getHttpServletRequest().setPreferredLocales(Arrays.stream(values).toList());
        return this;
    }

    public MockRequestContext setActiveFlow(final Flow flow) {
        setFlowExecutionContext(new MockFlowExecutionContext(flow));
        return this;
    }

    public MockRequestContext setHttpRequestCookies(final Cookie... cookies) {
        getHttpServletRequest().setCookies(cookies);
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

    public MockRequestContext setContextPath(final String contextPath) {
        getHttpServletRequest().setContextPath(contextPath);
        getMockExternalContext().setContextPath(contextPath);
        return this;
    }

    public MockRequestContext addHeader(final String name, final Object value) {
        getHttpServletRequest().addHeader(name, value);
        return this;
    }

    public MockRequestContext setRemoteAddr(final String remoteAddr) {
        getHttpServletRequest().setRemoteAddr(remoteAddr);
        return this;
    }

    public MockRequestContext setServerName(final String host) {
        getHttpServletRequest().setServerName(host);
        return this;
    }
    
    public MockRequestContext setServletPath(final String path) {
        getHttpServletRequest().setServletPath(path);
        return this;
    }

    public MockRequestContext setLocalAddr(final String localAddr) {
        getHttpServletRequest().setLocalAddr(localAddr);
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

    public MockRequestContext setRequestURI(final String uri) {
        getHttpServletRequest().setRequestURI(uri);
        return this;
    }

    public MockRequestContext setRequestCookiesFromResponse() {
        getHttpServletRequest().setCookies(getHttpServletResponse().getCookies());
        return this;
    }

    public MockRequestContext withUserAgent() {
        withUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64)");
        return this;
    }

    @CanIgnoreReturnValue
    public MockRequestContext withUserAgent(final String s) {
        addHeader(HttpHeaders.USER_AGENT, s);
        return this;
    }

    public MockRequestContext setRequestAttribute(final String name, final Object value) {
        getHttpServletRequest().setAttribute(name, value);
        return this;
    }

    public MockRequestContext withLocale(final Locale value) {
        getHttpServletRequest().setAttribute("locale", value);
        setParameter("locale", value.toLanguageTag());
        ((MockExternalContext) getExternalContext()).setLocale(value);
        return this;
    }

    public MockRequestContext setQueryString(final String s) {
        getHttpServletRequest().setQueryString(s);
        return this;
    }

    public MockRequestContext setFlowExecutionContext(final String flowIdLogin) {
        val flow = new Flow(flowIdLogin);
        flow.setApplicationContext(getApplicationContext());
        val exec = new MockFlowExecutionContext(new MockFlowSession(flow));
        setFlowExecutionContext(exec);
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
        externalContext.setLocale(Locale.ENGLISH);
        requestContext.setExternalContext(externalContext);
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(externalContext);
        if (applicationContext != null) {
            val flow = (Flow) requestContext.getActiveFlow();
            flow.setApplicationContext(applicationContext);
        }
        return requestContext;
    }

    public MockRequestContext setClientInfo() {
        ClientInfoHolder.setClientInfo(ClientInfo.from(getHttpServletRequest()));
        return this;
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
