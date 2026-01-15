package org.apereo.cas.web.flow.resolver;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DynamicTargetStateResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Webflow")
class DynamicTargetStateResolverTests {
    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create();
        val rootFlow = context.getRootFlow();
        WebUtils.putTargetState(context, rootFlow.getStartState().getId());
        val resolver = new DynamicTargetStateResolver(rootFlow);
        val result = resolver.resolveTargetState(mock(Transition.class), mock(State.class), context);
        assertEquals(rootFlow.getStartState().getId(), result.getId());
    }
}
