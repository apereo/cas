package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link StatelessTicketRegistryWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class StatelessTicketRegistryWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public StatelessTicketRegistryWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final FlowDefinitionRegistry flowDefinitionRegistry,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        val storageType = casProperties.getTicket().getRegistry().getStateless().getStorageType();
        val setStorageTypeAction = createSetAction(
            "requestScope.%s".formatted(BrowserStorage.BrowserStorageTypes.class.getSimpleName()),
            "'%s'".formatted(storageType));

        val loginFlow = getLoginFlow();
        prependActionsToActionStateExecutionList(loginFlow,
            CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK,
            ConsumerExecutionAction.wrap(setStorageTypeAction),
            CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE);

        val writeStorage = getState(loginFlow, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_WRITE, ViewState.class);
        prependActionsToActionStateExecutionList(loginFlow, writeStorage, setStorageTypeAction);

        val flow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        prependActionsToActionStateExecutionList(flow,
            CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
            ConsumerExecutionAction.wrap(setStorageTypeAction),
            CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE);
    }
}
