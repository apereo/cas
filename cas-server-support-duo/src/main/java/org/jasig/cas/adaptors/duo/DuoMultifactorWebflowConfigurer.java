package org.jasig.cas.adaptors.duo;

import org.jasig.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.binding.mapping.Mapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowAttributeMapper;
import org.springframework.webflow.engine.SubflowState;

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

    private static final String MFA_SUCCESS_EVENT_ID = "mfaSuccess";
    private static final String MFA_DUO_EVENT_ID = "mfa-duo";

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final SubflowState subflowState = createSubflowState(flow, MFA_DUO_EVENT_ID, MFA_DUO_EVENT_ID);

        final List<DefaultMapping> mappings = new ArrayList<>();
        final Mapper inputMapper = createMapperToSubflowState(mappings);
        final SubflowAttributeMapper subflowMapper = createSubflowAttributeMapper(inputMapper, null);
        subflowState.setAttributeMapper(subflowMapper);
        subflowState.getTransitionSet().add(createTransition(MFA_SUCCESS_EVENT_ID,
                TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));

        final ActionState actionState = (ActionState) flow.getState(TRANSITION_ID_REAL_SUBMIT);
        logger.debug("Retrieved action state {}", actionState.getId());
        createTransitionForState(actionState, MFA_DUO_EVENT_ID, MFA_DUO_EVENT_ID);

    }
}
