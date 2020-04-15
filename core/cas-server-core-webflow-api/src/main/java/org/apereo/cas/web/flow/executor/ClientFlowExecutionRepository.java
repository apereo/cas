package org.apereo.cas.web.flow.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionKeyFactory;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

import java.io.Serializable;

/**
 * Stores all flow execution state in {@link ClientFlowExecutionKey}, which effectively stores execution state on the
 * client in a form parameter when a view is rendered. The details of encoding flow state into a byte stream is handled
 * by a {@link Transcoder} component.
 *
 * @author Marvin S. Addison
 * @see ClientFlowExecutionKey
 * @see Transcoder
 * @since 6.1
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientFlowExecutionRepository implements FlowExecutionRepository, FlowExecutionKeyFactory {

    /**
     * Client flow storage has not backing store independent from the flow key, so no locking is required.
     */
    private static final FlowExecutionLock NOOP_LOCK = new FlowExecutionLock() {
        @Override
        public void lock() {
        }

        @Override
        public void unlock() {
        }
    };

    private FlowExecutionFactory flowExecutionFactory;

    private FlowDefinitionLocator flowDefinitionLocator;

    private Transcoder transcoder;

    @Override
    public FlowExecutionKey parseFlowExecutionKey(final String encodedKey) throws FlowExecutionRepositoryException {
        return ClientFlowExecutionKey.parse(encodedKey);
    }

    @Override
    public FlowExecutionLock getLock(final FlowExecutionKey key) throws FlowExecutionRepositoryException {
        return NOOP_LOCK;
    }

    @Override
    public FlowExecution getFlowExecution(final FlowExecutionKey key) throws FlowExecutionRepositoryException {
        Assert.notNull(flowExecutionFactory, "FlowExecutionFactory cannot be null");
        Assert.notNull(flowDefinitionLocator, "FlowDefinitionLocator cannot be null");
        Assert.notNull(transcoder, "Transcoder cannot be null");

        if (!(key instanceof ClientFlowExecutionKey)) {
            throw new IllegalArgumentException(
                "Expected instance of ClientFlowExecutionKey but got " + key.getClass().getName());
        }
        try {
            val encoded = ((ClientFlowExecutionKey) key).getData();
            val state = (SerializedFlowExecutionState) this.transcoder.decode(encoded);
            val flow = flowDefinitionLocator.getFlowDefinition(state.getFlowId());
            return flowExecutionFactory.restoreFlowExecution(
                state.getExecution(), flow, key, state.getConversationScope(), this.flowDefinitionLocator);
        } catch (final Exception e) {
            throw new ClientFlowExecutionRepositoryException("Error decoding flow execution", e);
        }
    }

    @Override
    public void putFlowExecution(final FlowExecution flowExecution) throws FlowExecutionRepositoryException {
    }

    @Override
    public void removeFlowExecution(final FlowExecution flowExecution) throws FlowExecutionRepositoryException {
    }

    @Override
    public FlowExecutionKey getKey(final FlowExecution execution) {
        try {
            return new ClientFlowExecutionKey(this.transcoder.encode(new SerializedFlowExecutionState(execution)));
        } catch (final Exception e) {
            throw new ClientFlowExecutionRepositoryException("Error encoding flow execution", e);
        }
    }

    @Override
    public void updateFlowExecutionSnapshot(final FlowExecution execution) {
    }

    @Override
    public void removeFlowExecutionSnapshot(final FlowExecution execution) {
    }

    @Override
    public void removeAllFlowExecutionSnapshots(final FlowExecution execution) {
    }

    @Getter
    private static class SerializedFlowExecutionState implements Serializable {
        private static final long serialVersionUID = -4020991769174829876L;

        private final String flowId;

        private final MutableAttributeMap conversationScope;

        private final FlowExecution execution;

        SerializedFlowExecutionState(final FlowExecution execution) {
            this.execution = execution;
            this.flowId = execution.getDefinition().getId();
            this.conversationScope = execution.getConversationScope();
        }
    }
}
