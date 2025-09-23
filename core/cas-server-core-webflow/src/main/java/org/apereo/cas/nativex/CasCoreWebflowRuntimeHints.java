package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.configurer.CasWebflowCustomizer;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import org.apereo.cas.web.flow.executor.CasFlowExecutor;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepository;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.impl.ConversationContainer;
import org.springframework.webflow.core.AnnotatedObject;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.FlowExecutionExceptionHandlerSet;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.repository.impl.FlowExecutionSnapshotGroup;
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
        registerProxyHints(hints, List.of(
            Action.class,
            FlowDefinitionRegistry.class,
            TransitionDefinition.class,
            StateDefinition.class,
            CasWebflowConfigurer.class,
            CasWebflowCustomizer.class,
            CasWebflowEventResolver.class,
            WebflowDecorator.class,
            CasWebflowExecutionPlanConfigurer.class,
            CasWebflowExceptionHandler.class
        ));
        registerSpringProxyHints(hints, CasFlowExecutor.class);
        registerSpringProxyHints(hints, CasDelegatingWebflowEventResolver.class, CasWebflowEventResolver.class);
        registerSerializationHints(hints,
            ClientFlowExecutionRepository.SerializedFlowExecutionState.class,
            ConversationContainer.class,
            ConversationId.class,
            LocalAttributeMap.class
        );

        registerSerializationHints(hints, findSubclassesInPackage(FlowExecutionSnapshotGroup.class, "org.springframework.webflow"));
        registerSerializationHints(hints, findSubclassesInPackage(Conversation.class, "org.springframework.webflow"));
        registerSerializationHints(hints, findSubclassesOf(FlowExecutionKey.class));

        registerReflectionHints(hints, findSubclassesInPackage(MessageContext.class, "org.springframework.binding"));
        registerReflectionHints(hints, findSubclassesInPackage(ValidationContext.class, "org.springframework.binding"));
        registerReflectionHints(hints, findSubclassesInPackage(RequestContext.class, "org.springframework.webflow"));
        registerReflectionHints(hints, findSubclassesInPackage(FlowSession.class, "org.springframework.webflow"));
        registerReflectionHints(hints, findSubclassesInPackage(ViewFactoryCreator.class, "org.springframework.webflow"));
        registerReflectionHints(hints, findSubclassesInPackage(FlowExecution.class, "org.springframework.webflow"));

        registerReflectionHints(hints, List.of(
            CasWebflowEventResolver.class,
            TransitionSet.class,
            FlowDefinitionRegistry.class,
            FlowExecutionExceptionHandlerSet.class
        ));

        registerReflectionHints(hints,
            findSubclassesInPackage(AnnotatedObject.class, "org.springframework.webflow"));

    }
}
