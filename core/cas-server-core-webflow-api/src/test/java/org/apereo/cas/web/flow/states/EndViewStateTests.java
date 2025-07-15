package org.apereo.cas.web.flow.states;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EndViewStateTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Webflow")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class EndViewStateTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val state = new EndViewState(new Flow("flow"), "end",
            new ActionExecutingViewFactory(ConsumerExecutionAction.OK));
        state.enter(context);
        assertTrue(context.getFlowExecutionContext().hasEnded());
    }

    @Test
    void verifyForceViewRendering() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val state = new EndViewState(new Flow("flow"), "end",
            new ActionExecutingViewFactory(ConsumerExecutionAction.OK));
        state.setForceRenderView(true);
        state.enter(context);
        val currentView = context.getCurrentView();
        assertNotNull(currentView);
        assertTrue(context.getFlowExecutionContext().hasEnded());
    }
}
