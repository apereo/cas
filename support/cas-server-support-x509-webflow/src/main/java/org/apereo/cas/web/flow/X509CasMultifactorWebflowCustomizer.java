package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

/**
 * This is {@link X509CasMultifactorWebflowCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class X509CasMultifactorWebflowCustomizer implements CasMultifactorWebflowCustomizer {
    @Override
    public Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return List.of(CasWebflowConstants.STATE_ID_X509_START);
    }
}
