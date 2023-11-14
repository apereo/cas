package org.apereo.cas.nativex;

import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.configurer.CasWebflowCustomizer;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepository;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.webflow.core.AnnotatedObject;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.engine.FlowExecutionExceptionHandlerSet;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.Action;
import java.util.Collection;
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
            .registerJdkProxy(TransitionDefinition.class)
            .registerJdkProxy(StateDefinition.class)
            .registerJdkProxy(CasWebflowConfigurer.class)
            .registerJdkProxy(CasWebflowCustomizer.class)
            .registerJdkProxy(WebflowDecorator.class)
            .registerJdkProxy(CasWebflowExecutionPlanConfigurer.class)
            .registerJdkProxy(CasWebflowExceptionHandler.class);

        hints.serialization()
            .registerType(ClientFlowExecutionRepository.SerializedFlowExecutionState.class)
            .registerType(LocalAttributeMap.class);

        registerReflectionHints(hints,
            findSubclassesInPackage(MessageContext.class, "org.springframework.binding"));
        registerReflectionHints(hints,
            findSubclassesInPackage(ValidationContext.class, "org.springframework.binding"));

        registerReflectionHints(hints, List.of(
            TypeReference.of("org.springframework.webflow.engine.impl.RequestControlContextImpl"),
            TypeReference.of("org.springframework.webflow.engine.impl.FlowSessionImpl"),
            TransitionSet.class,
            FlowExecutionImpl.class,
            FlowExecutionExceptionHandlerSet.class,
            WebflowConversationStateCipherExecutor.class
        ));

        registerReflectionHints(hints,
            findSubclassesInPackage(AnnotatedObject.class, "org.springframework.webflow"));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> {
            if (el instanceof final String clazz) {
                hints.reflection().registerType(TypeReference.of(clazz), memberCategories);
            }
            if (el instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
            if (el instanceof final TypeReference reference) {
                hints.reflection().registerType(reference, memberCategories);
            }
        });
    }
}
