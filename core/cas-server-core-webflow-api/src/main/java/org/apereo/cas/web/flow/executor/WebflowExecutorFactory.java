package org.apereo.cas.web.flow.executor;

import org.apereo.cas.configuration.model.core.web.flow.WebflowProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
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

    private final FlowUrlHandler flowUrlHandler;

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
        val session = webflowProperties.getSession().getServer();
        conversationManager.setLockTimeoutSeconds((int) Beans.newDuration(session.getLockTimeout()).toSeconds());
        conversationManager.setMaxConversations(session.getMaxConversations());

        val executionFactory = new FlowExecutionImplFactory();
        executionFactory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));

        val flowExecutionSnapshotFactory = new SerializedFlowExecutionSnapshotFactory(executionFactory, flowDefinitionRegistry);
        flowExecutionSnapshotFactory.setCompress(session.isCompress());

        val repository = new DefaultFlowExecutionRepository(conversationManager, flowExecutionSnapshotFactory);
        executionFactory.setExecutionKeyFactory(repository);
        return buildCasFlowExecutor(executionFactory, repository);
    }

    private FlowExecutor buildFlowExecutorViaClientFlowExecution() {
        val repository = new ClientFlowExecutionRepository();
        repository.setWebflowProperties(webflowProperties);
        repository.setFlowDefinitionLocator(flowDefinitionRegistry);
        repository.setTranscoder(getWebflowStateTranscoder());

        val executionFactory = new FlowExecutionImplFactory();
        executionFactory.setExecutionKeyFactory(repository);
        repository.setFlowExecutionFactory(executionFactory);
        executionFactory.setExecutionListenerLoader(new StaticFlowExecutionListenerLoader(executionListeners));
        return buildCasFlowExecutor(executionFactory, repository);
    }

    private CasFlowExecutorImpl buildCasFlowExecutor(final FlowExecutionFactory executionFactory, final FlowExecutionRepository repository) {
        return new CasFlowExecutorImpl(new FlowExecutorImpl(flowDefinitionRegistry, executionFactory, repository), repository, flowUrlHandler);
    }

    private Transcoder getWebflowStateTranscoder() {
        val cipherBean = new WebflowCipherBean(webflowCipherExecutor);
        return new EncryptedTranscoder(cipherBean);
    }

    @RequiredArgsConstructor
    @Getter
    static class CasFlowExecutorImpl implements CasFlowExecutor {
        @Delegate(types = FlowExecutor.class)
        private final FlowExecutorImpl flowExecutor;

        private final FlowExecutionRepository flowExecutionRepository;

        private final FlowUrlHandler flowUrlHandler;
    }
}
