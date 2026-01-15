package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

/**
 * This is {@link SpnegoCasMultifactorWebflowCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class SpnegoCasMultifactorWebflowCustomizer implements CasMultifactorWebflowCustomizer {
    @Override
    public Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return List.of(CasWebflowConstants.STATE_ID_SPNEGO);
    }
}
