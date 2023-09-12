package org.apereo.cas.util;

import lombok.val;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import java.util.Objects;
import static org.mockito.Mockito.*;

/**
 * This is {@link MockRequestContext}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class MockRequestContext extends org.springframework.webflow.test.MockRequestContext implements RequestControlContext {
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

    @Override
    public void setCurrentState(final State state) {
        
    }

    @Override
    public FlowExecutionKey assignFlowExecutionKey() {
        return null;
    }

    @Override
    public void viewRendering(final View view) {
    }

    @Override
    public void viewRendered(final View view) {
    }

    @Override
    public boolean handleEvent(final Event event) throws FlowExecutionException {
        return false;
    }

    @Override
    public boolean execute(final Transition transition) {
        return false;
    }

    @Override
    public void updateCurrentFlowExecutionSnapshot() {
    }

    @Override
    public void removeCurrentFlowExecutionSnapshot() {
    }

    @Override
    public void removeAllFlowExecutionSnapshots() {

    }

    @Override
    public void start(final Flow flow, final MutableAttributeMap<?> mutableAttributeMap) throws FlowExecutionException {
    }

    @Override
    public void endActiveFlowSession(final String s, final MutableAttributeMap<Object> mutableAttributeMap) throws IllegalStateException {
    }

    @Override
    public boolean getRedirectOnPause() {
        return false;
    }

    @Override
    public boolean getRedirectInSameState() {
        return false;
    }

    @Override
    public boolean getEmbeddedMode() {
        return false;
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
}
