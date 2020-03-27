package org.apereo.cas.web.flow.executor;

import org.apereo.cas.configuration.model.core.web.flow.WebflowProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
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
        val conversationManager = new SessionBindingConversationManager();
        val session = webflowProperties.getSession();
        conversationManager.setLockTimeoutSeconds((int) Beans.newDuration(session.getLockTimeout()).getSeconds());
        conversationManager.setMaxConversations(session.getMaxConversations());

        val executionFactory = new FlowExecutionImplFactory();
        executionFactory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));

        val flowExecutionSnapshotFactory =
            new SerializedFlowExecutionSnapshotFactory(executionFactory, this.flowDefinitionRegistry);
        flowExecutionSnapshotFactory.setCompress(session.isCompress());

        val repository = new DefaultFlowExecutionRepository(conversationManager, flowExecutionSnapshotFactory);
        executionFactory.setExecutionKeyFactory(repository);
        return new FlowExecutorImpl(this.flowDefinitionRegistry, executionFactory, repository);
    }

    private FlowExecutor buildFlowExecutorViaClientFlowExecution() {
        val repository = new ClientFlowExecutionRepository();
        repository.setFlowDefinitionLocator(this.flowDefinitionRegistry);
        repository.setTranscoder(getWebflowStateTranscoder());

        val factory = new FlowExecutionImplFactory();
        factory.setExecutionKeyFactory(repository);
        factory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader());
        repository.setFlowExecutionFactory(factory);
        factory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));
        return new FlowExecutorImpl(this.flowDefinitionRegistry, factory, repository);
    }

    @SneakyThrows
    private Transcoder getWebflowStateTranscoder() {
        val cipherBean = new WebflowCipherBean(this.webflowCipherExecutor);
        return new EncryptedTranscoder(cipherBean);
    }
}
