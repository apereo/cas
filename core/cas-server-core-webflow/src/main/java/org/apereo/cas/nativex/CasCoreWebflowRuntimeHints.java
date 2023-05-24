package org.apereo.cas.nativex;

import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepository;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link CasCoreWebflowRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreWebflowRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies()
            .registerJdkProxy(Action.class)
            .registerJdkProxy(CasWebflowConfigurer.class)
            .registerJdkProxy(WebflowDecorator.class)
            .registerJdkProxy(CasWebflowExecutionPlanConfigurer.class)
            .registerJdkProxy(CasWebflowExceptionHandler.class);

        hints.serialization()
            .registerType(ClientFlowExecutionRepository.SerializedFlowExecutionState.class)
            .registerType(LocalAttributeMap.class);

        List.of(
                "org.springframework.webflow.engine.impl.RequestControlContextImpl",
                "org.springframework.webflow.engine.impl.FlowSessionImpl",
                FlowExecutionImpl.class.getName(),
                WebflowConversationStateCipherExecutor.class.getName()
            )
            .forEach(el -> hints.reflection().registerTypeIfPresent(classLoader, el,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }
}
