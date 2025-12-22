package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedClientAuthenticationCredentialSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedClientAuthenticationCredentialSelectionAction extends BaseCasWebflowAction {
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val profiles = DelegationWebflowUtils.getDelegatedClientAuthenticationResolvedCredentials(requestContext,
            DelegatedAuthenticationCandidateProfile.class);
        if (profiles.size() == 1) {
            val profile = profiles.getFirst();
            DelegationWebflowUtils.putDelegatedClientAuthenticationCandidateProfile(requestContext, profile);
            return new Event(this, CasWebflowConstants.TRANSITION_ID_FINALIZE,
                new LocalAttributeMap<>("profile", profile));
        }
        return new Event(this, CasWebflowConstants.TRANSITION_ID_SELECT);
    }
}
