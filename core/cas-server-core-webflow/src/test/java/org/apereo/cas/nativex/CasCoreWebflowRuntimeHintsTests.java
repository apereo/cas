package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepository;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link CasCoreWebflowRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreWebflowRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasCoreWebflowRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(CasWebflowConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(ClientFlowExecutionRepository.SerializedFlowExecutionState.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(FlowExecutionImpl.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(DefaultMessageContext.class).test(hints));
    }
}
