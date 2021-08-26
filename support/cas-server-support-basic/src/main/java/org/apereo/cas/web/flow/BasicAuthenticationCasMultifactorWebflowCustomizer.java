package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link BasicAuthenticationCasMultifactorWebflowCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class BasicAuthenticationCasMultifactorWebflowCustomizer implements CasMultifactorWebflowCustomizer {
    @Override
    public Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return List.of(BasicAuthenticationWebflowConfigurer.STATE_ID_BASIC_AUTHENTICATION_CHECK);
    }
}
