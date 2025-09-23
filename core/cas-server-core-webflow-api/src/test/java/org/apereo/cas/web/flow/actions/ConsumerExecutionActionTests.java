package org.apereo.cas.web.flow.actions;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConsumerExecutionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
class ConsumerExecutionActionTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val action = new ConsumerExecutionAction(RequestContext::getFlowScope).setEventId("result");
        assertNotNull(action.execute(context));
        assertNotNull(action.toString());
    }

    @Test
    void verifyNoContentOperation() throws Throwable {
        val context = MockRequestContext.create();
        val result = ConsumerExecutionAction.NO_CONTENT.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.NO_CONTENT.value(), context.getHttpServletResponse().getStatus());
        assertNotNull(WebUtils.getActiveFlow(context));
    }

    @Test
    void verifyEventAttributesOperation() throws Throwable {
        val context = MockRequestContext.create();
        context.setCurrentEvent(new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            new LocalAttributeMap<>(Map.of("key", "value"))));
        val result = ConsumerExecutionAction.EVENT_ATTRIBUTES_TO_FLOW_SCOPE.execute(context);
        assertNull(result);
        assertEquals("value", context.getFlowScope().get("key"));
    }
}
