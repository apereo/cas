package org.apereo.cas.web.flow.executor;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.webapp.WebflowProperties;
import org.apereo.cas.configuration.model.webapp.WebflowSessionManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.spring.webflow.plugin.ClientFlowExecutionRepository;
import org.apereo.spring.webflow.plugin.EncryptedTranscoder;
import org.apereo.spring.webflow.plugin.Transcoder;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
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
@AllArgsConstructor
public class WebflowExecutorFactory {
    private final WebflowProperties webflowProperties;
    private final FlowDefinitionRegistry flowDefinitionRegistry;
    private final CipherExecutor webflowCipherExecutor;

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
        final SessionBindingConversationManager conversationManager = new SessionBindingConversationManager();
        final WebflowSessionManagementProperties session = webflowProperties.getSession();
        conversationManager.setLockTimeoutSeconds((int) Beans.newDuration(session.getLockTimeout()).toMillis());
        conversationManager.setMaxConversations(session.getMaxConversations());

        final FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();
        final SerializedFlowExecutionSnapshotFactory flowExecutionSnapshotFactory =
            new SerializedFlowExecutionSnapshotFactory(executionFactory, this.flowDefinitionRegistry);
        flowExecutionSnapshotFactory.setCompress(session.isCompress());

        final DefaultFlowExecutionRepository repository = new DefaultFlowExecutionRepository(conversationManager,
            flowExecutionSnapshotFactory);
        executionFactory.setExecutionKeyFactory(repository);
        return new FlowExecutorImpl(this.flowDefinitionRegistry, executionFactory, repository);
    }

    private FlowExecutor buildFlowExecutorViaClientFlowExecution() {
        final ClientFlowExecutionRepository repository = new ClientFlowExecutionRepository();
        repository.setFlowDefinitionLocator(this.flowDefinitionRegistry);
        repository.setTranscoder(getWebflowStateTranscoder());

        final FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
        factory.setExecutionKeyFactory(repository);
        repository.setFlowExecutionFactory(factory);
        return new FlowExecutorImpl(this.flowDefinitionRegistry, factory, repository);
    }

    @SneakyThrows
    private Transcoder getWebflowStateTranscoder() {
        return new EncryptedTranscoder(new WebflowCipherBean(this.webflowCipherExecutor));
    }
}
