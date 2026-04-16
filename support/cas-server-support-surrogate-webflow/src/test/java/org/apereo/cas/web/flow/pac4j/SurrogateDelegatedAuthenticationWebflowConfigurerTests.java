package org.apereo.cas.web.flow.pac4j;

import module java.base;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateDelegatedAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Delegation")
@ImportAutoConfiguration({
    CasSurrogateAuthenticationWebflowAutoConfiguration.class,
    CasDelegatedAuthenticationAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pac4j.core.allow-impersonation=true",
    "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate"
})
@ExtendWith(CasTestExtension.class)
class SurrogateDelegatedAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        val requestContext = MockRequestContext.create(applicationContext);
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
        assertNotNull(state);
        requestContext.setCurrentState(state);
        requestContext.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS));
        state.getExitActionList().forEach(Unchecked.consumer(action -> action.execute(requestContext)));
        assertTrue(WebUtils.hasSurrogateAuthenticationRequest(requestContext));
    }
}
