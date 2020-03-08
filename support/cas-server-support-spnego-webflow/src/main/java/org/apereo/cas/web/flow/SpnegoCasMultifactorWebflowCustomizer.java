package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import java.util.Collection;
import java.util.List;

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
