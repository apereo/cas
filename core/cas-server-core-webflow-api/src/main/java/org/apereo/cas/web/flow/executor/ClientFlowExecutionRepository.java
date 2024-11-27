package org.apereo.cas.web.flow.executor;

import org.apereo.cas.configuration.model.core.web.flow.WebflowProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
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
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

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
@Slf4j
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

    private static final String WEBFLOW_USER_AGENT = ClientFlowExecutionKey.class.getName() + ".userAgent";
    private static final String WEBFLOW_CLIENT_IP_ADDRESS = ClientFlowExecutionKey.class.getName() + ".clientIpAddress";

    private FlowExecutionFactory flowExecutionFactory;

    private FlowDefinitionLocator flowDefinitionLocator;

    private Transcoder transcoder;

    private WebflowProperties webflowProperties;

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
        Assert.notNull(webflowProperties, "Webflow properties cannot be null");

        if (!(key instanceof ClientFlowExecutionKey)) {
            throw new IllegalArgumentException(
                "Expected instance of ClientFlowExecutionKey but got " + key.getClass().getName());
        }
        try {
            val encoded = ((ClientFlowExecutionKey) key).getData();
            val state = (SerializedFlowExecutionState) transcoder.decode(encoded);

            if (webflowProperties.getSession().isPinToSession()) {
                verifyWebflowSessionIsCorrectlyPinned(state);
            }
            val conversationScope = state.getConversationScope();
            val flow = flowDefinitionLocator.getFlowDefinition(state.getFlowId());
            return flowExecutionFactory.restoreFlowExecution(state.getExecution(), flow, key, conversationScope, this.flowDefinitionLocator);
        } catch (final Exception e) {
            throw new ClientFlowExecutionRepositoryException("Error decoding flow execution", e);
        }
    }

    protected void verifyWebflowSessionIsCorrectlyPinned(final SerializedFlowExecutionState state) {
        val currentClientInfo = ClientInfoHolder.getClientInfo();

        val conversationScope = state.getConversationScope();
        val userAgent = (String) conversationScope.get(WEBFLOW_USER_AGENT);
        val clientIpAddress = (String) conversationScope.get(WEBFLOW_CLIENT_IP_ADDRESS);
        Assert.hasText(userAgent, "User-agent cannot be null or empty");
        Assert.hasText(clientIpAddress, "Client IP address cannot be null or empty");

        if (!StringUtils.equals(currentClientInfo.getUserAgent(), userAgent)
            || !StringUtils.equals(currentClientInfo.getClientIpAddress(), clientIpAddress)) {
            LOGGER.error("User-agent attached to the webflow [{}] does not match the current user-agent [{}] or "
                    + "client IP address attached to the webflow [{}] does not match the current client IP address [{}]. "
                    + "The flow execution key is invalid or likely tampered with.",
                userAgent, currentClientInfo.getUserAgent(), clientIpAddress, currentClientInfo.getClientIpAddress());
            throw new ClientFlowExecutionRepositoryException("Webflow execution key is invalid");
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
            if (webflowProperties.getSession().isPinToSession()) {
                recordWebflowSessionPinningInfo(execution);
            }
            val state = new SerializedFlowExecutionState(execution);
            return new ClientFlowExecutionKey(transcoder.encode(state));
        } catch (final Exception e) {
            throw new ClientFlowExecutionRepositoryException("Error encoding flow execution", e);
        }
    }

    protected void recordWebflowSessionPinningInfo(final FlowExecution execution) {
        val clientInfo = Objects.requireNonNull(ClientInfoHolder.getClientInfo(), "Client info cannot be null");
        Assert.hasText(clientInfo.getUserAgent(), "User-agent cannot be null or empty");
        Assert.hasText(clientInfo.getClientIpAddress(), "Client IP address cannot be null or empty");
        execution.getConversationScope().put(WEBFLOW_USER_AGENT, clientInfo.getUserAgent());
        execution.getConversationScope().put(WEBFLOW_CLIENT_IP_ADDRESS, clientInfo.getClientIpAddress());
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
    public static class SerializedFlowExecutionState implements Serializable {
        @Serial
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
