package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import java.util.Collection;
import java.util.List;

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
