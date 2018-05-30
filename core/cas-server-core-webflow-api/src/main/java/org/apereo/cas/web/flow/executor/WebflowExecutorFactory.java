package org.apereo.cas.web.flow.executor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.webapp.WebflowProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.spring.webflow.plugin.ClientFlowExecutionRepository;
import org.apereo.spring.webflow.plugin.EncryptedTranscoder;
import org.apereo.spring.webflow.plugin.Transcoder;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.impl.DefaultFlowExecutionRepository;
import org.springframework.webflow.execution.repository.snapshot.SerializedFlowExecutionSnapshotFactory;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * This is {@link WebflowExecutorFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class WebflowExecutorFactory {
    private final WebflowProperties webflowProperties;
    private final FlowDefinitionRegistry flowDefinitionRegistry;
    private final CipherExecutor webflowCipherExecutor;
    private final FlowExecutionListener[] executionListeners;

    /**
     * Build flow executor.
     *
     * @return the flow executor
     */
    public FlowExecutor build() {
        if (webflowProperties.getSession().isStorage()) {
            return buildFlowExecutorViaServerSessionBindingExecution();
        }
        return buildFlowExecutorViaClientFlowExecution();
    }

    private FlowExecutor buildFlowExecutorViaServerSessionBindingExecution() {
        final var conversationManager = new SessionBindingConversationManager();
        final var session = webflowProperties.getSession();
        conversationManager.setLockTimeoutSeconds((int) Beans.newDuration(session.getLockTimeout()).getSeconds());
        conversationManager.setMaxConversations(session.getMaxConversations());

        final var executionFactory = new FlowExecutionImplFactory();
        executionFactory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));

        final var flowExecutionSnapshotFactory =
            new SerializedFlowExecutionSnapshotFactory(executionFactory, this.flowDefinitionRegistry);
        flowExecutionSnapshotFactory.setCompress(session.isCompress());

        final var repository = new DefaultFlowExecutionRepository(conversationManager, flowExecutionSnapshotFactory);
        executionFactory.setExecutionKeyFactory(repository);
        return new FlowExecutorImpl(this.flowDefinitionRegistry, executionFactory, repository);
    }

    private FlowExecutor buildFlowExecutorViaClientFlowExecution() {
        final var repository = new ClientFlowExecutionRepository();
        repository.setFlowDefinitionLocator(this.flowDefinitionRegistry);
        repository.setTranscoder(getWebflowStateTranscoder());

        final var factory = new FlowExecutionImplFactory();
        factory.setExecutionKeyFactory(repository);
        factory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader());
        repository.setFlowExecutionFactory(factory);
        factory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));
        return new FlowExecutorImpl(this.flowDefinitionRegistry, factory, repository);
    }

    @SneakyThrows
    private Transcoder getWebflowStateTranscoder() {
        final var cipherBean = new WebflowCipherBean(this.webflowCipherExecutor);
        return new EncryptedTranscoder(cipherBean);
    }
}
