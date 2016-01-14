package org.jasig.cas.adaptors.duo;

import org.jasig.cas.web.flow.AbstractCasWebflowConfigurer;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.mapping.Mapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowAttributeMapper;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.builder.DefaultFlowHolder;
import org.springframework.webflow.engine.builder.FlowAssembler;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DuoMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoMultifactorWebflowConfigurer")
public class DuoMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /** Duo Webflow event id. */
    public static final String MFA_DUO_EVENT_ID = "mfa-duo";

    @Autowired
    @Qualifier("duoFlowRegistry")
    private FlowDefinitionRegistry duoFlowRegistry;

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final SubflowState subflowState = createSubflowState(flow, MFA_DUO_EVENT_ID, MFA_DUO_EVENT_ID);

        final List<DefaultMapping> mappings = new ArrayList<>();
        final Mapper inputMapper = createMapperToSubflowState(mappings);
        final SubflowAttributeMapper subflowMapper = createSubflowAttributeMapper(inputMapper, null);
        subflowState.setAttributeMapper(subflowMapper);
        subflowState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));

        final ActionState actionState = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
        logger.debug("Retrieved action state {}", actionState.getId());
        createTransitionForState(actionState, MFA_DUO_EVENT_ID, MFA_DUO_EVENT_ID);

        registerFlowDefinitionIntoLoginFlowRegistry(this.duoFlowRegistry);
    }
}
