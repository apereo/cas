package org.jasig.cas.web.flow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.impl.SimpleConversationId;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.execution.repository.snapshot.FlowExecutionSnapshotFactory;
import org.springframework.webflow.execution.repository.snapshot.SimpleFlowExecutionSnapshotFactory;
import org.springframework.webflow.test.MockAction;
import org.springframework.webflow.test.MockExternalContext;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.4.7
 */
public class CasFlowExecutionKeyFactoryTests {
    
    private ConversationManager mockConversationManager;

    @Before
    public void setUp() {
        final Map<Object, Object> attributes = new HashMap<Object, Object>();
        final Conversation mockConversation = mock(Conversation.class);
        when(mockConversation.getId()).thenReturn(new SimpleConversationId("ABC123"));
        when(mockConversation.getAttribute(anyString())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return attributes.get(invocation.getArguments()[0]);
            }
        });
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                return attributes.put(args[0], args[1]);
            }
        }).when(mockConversation).putAttribute(anyObject(), anyObject());

        this.mockConversationManager = mock(ConversationManager.class);
        when(this.mockConversationManager.beginConversation((ConversationParameters) any())).thenReturn(mockConversation);
        when(this.mockConversationManager.getConversation((ConversationId) any())).thenReturn(mockConversation);
        when(this.mockConversationManager.parseConversationId(anyString())).thenAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) throws Throwable {
               return new SimpleConversationId((String) invocation.getArguments()[0]);
           }
        });
    }

    @Test
    public void testGenerateAndParseKey() throws Exception {
        final CasFlowExecutionKeyFactory keyFactory = new CasFlowExecutionKeyFactory(
            this.mockConversationManager,
            mock(FlowExecutionSnapshotFactory.class));
        final FlowExecutionKey key = keyFactory.getKey(newMockExecution(createSimpleFlow("test")));
        assertNotNull(key);
        assertEquals(key, keyFactory.parseFlowExecutionKey(key.toString()));
    }

    @Test
    public void testTamperResitantKey() throws Exception {
        final CasFlowExecutionKeyFactory keyFactory = new CasFlowExecutionKeyFactory(
            this.mockConversationManager,
            mock(FlowExecutionSnapshotFactory.class));
        final FlowExecutionKey key = keyFactory.getKey(newMockExecution(createSimpleFlow("test")));
        assertNotNull(key);
        // Tamper with the key by replacing first character of UUID component
        final char[] keyChars = key.toString().toCharArray();
        keyChars[3] = keyChars[3] == '0' ? 'f' : '0';
        final String tamperedKey = new String(keyChars);
        try {
            keyFactory.parseFlowExecutionKey(tamperedKey);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(key, keyFactory.parseFlowExecutionKey(key.toString()));
        }
    }
    
    @Test
    public void testUniquePerRequest() throws Exception {
        final CasFlowExecutionKeyFactory keyFactory = new CasFlowExecutionKeyFactory(
            this.mockConversationManager,
            mock(FlowExecutionSnapshotFactory.class));
        keyFactory.setAlwaysGenerateNewNextKey(true);
        final FlowExecution execution = newMockExecution(createSimpleFlow("test"));
        final FlowExecutionKey key1 = keyFactory.getKey(execution);
        final FlowExecutionKey key2 = keyFactory.getKey(execution);
        assertFalse(key1.equals(key2));
    }

    @Test
    public void testSubflow() throws Exception {
        final FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();
        final Flow flow = createFlowWithSubflow("parent", "child");
        final CasFlowExecutionKeyFactory keyFactory = new CasFlowExecutionKeyFactory(
            this.mockConversationManager,
            new SimpleFlowExecutionSnapshotFactory(executionFactory, newMockFlowLocator(flow)));
        keyFactory.setAlwaysGenerateNewNextKey(true);
        executionFactory.setExecutionKeyFactory(keyFactory);
        final FlowExecution execution = executionFactory.createFlowExecution(flow);

        execution.start(null, new MockExternalContext());

        // Flow state: rendered subflow view
        assertTrue(execution.isActive());
        assertEquals("view", execution.getActiveSession().getScope().get("renderCalled"));
        final FlowExecutionKey key1 = execution.getKey();
        assertNotNull(key1);
        assertEquals(key1, keyFactory.parseFlowExecutionKey(key1.toString()));
       
        final MockExternalContext context1 = new MockExternalContext();
        context1.setEventId("submit");
        execution.resume(context1);

        // Flow state: rendered parent view (following subflow termination)
        assertTrue(execution.isActive());
        assertEquals("parentview", execution.getActiveSession().getScope().get("renderCalled"));
        final FlowExecutionKey key2 = execution.getKey();
        assertNotNull(key2);
        assertFalse(key1.equals(key2));
        assertEquals(key2, keyFactory.parseFlowExecutionKey(key2.toString()));

        final MockExternalContext context2 = new MockExternalContext();
        context2.setEventId("submit");
        execution.resume(context2);

        // Flow state: completed
        assertTrue(execution.hasEnded());
    }
    
    private Flow createSimpleFlow(final String id) {
        // Create a flat flow with an action and single view state
        final Flow flow = new Flow(id);
        final ActionState state1 = new ActionState(flow, "state1-action");
        final ViewState state2 = new ViewState(flow, "state2-view", new MockViewFactory("view"));
        new EndState(flow, "state3-end");
        state1.getActionList().add(new MockAction("state1-result"));
        state1.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("state2-view")));
        state2.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("state3-end")));
        return flow;
    }
    
    private Flow createFlowWithSubflow(final String parentId, final String childId) {
        // Create a flow containing a subflow followed by a view state
        final Flow parent = new Flow(parentId);
        final Flow child = createSimpleFlow(childId);
        final ActionState state1 = new ActionState(parent, "state1-action");
        final SubflowState state2 = new SubflowState(parent, "state2-subflow", new StaticExpression(child));
        final ViewState state3 = new ViewState(parent, "state3-view", new MockViewFactory("parentview"));
        new EndState(parent, "state4-end");
        state1.getActionList().add(new MockAction("state1-result"));
        state1.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("state2-subflow")));
        state2.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("state3-view")));
        state3.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("state4-end")));
        return parent;
    }
    
    private FlowExecution newMockExecution(final Flow flow) {
        final FlowExecution execution = mock(FlowExecution.class);
        when(execution.getDefinition()).thenReturn(flow);
        return execution;
    }
    
    private FlowDefinitionLocator newMockFlowLocator(final Flow flow) {
        final FlowDefinitionLocator locator = mock(FlowDefinitionLocator.class);
        when(locator.getFlowDefinition(anyString())).thenReturn(flow);
        return locator;
    }
    
    static class MockViewFactory implements ViewFactory {
        private String id;
        
        public MockViewFactory(final String id) {
            this.id = id;
        }

        public View getView(RequestContext context) {
            return new TaggedView(context, this.id);
        }
    }
    
    static class TaggedView implements View {
        private RequestContext context;
        private String tag;

        public TaggedView(final RequestContext context, final String tag) {
            this.tag = tag;
            this.context = context;
        }

        public void render() {
            this.context.getFlowScope().put("renderCalled", this.tag);
        }

        public boolean userEventQueued() {
            return hasFlowEvent();
        }

        public void processUserEvent() {
            // Do nothing
        }

        public Serializable getUserEventState() {
            return "12345";
        }

        public boolean hasFlowEvent() {
            return this.context.getExternalContext().getRequestParameterMap().contains("_eventId");
        }

        public Event getFlowEvent() {
            return new Event(this, this.context.getExternalContext().getRequestParameterMap().get("_eventId"));
        }

        public void saveState() {
            this.context.getFlowScope().put("saveStateCalled", Boolean.TRUE);
        }
    }
    
    
}