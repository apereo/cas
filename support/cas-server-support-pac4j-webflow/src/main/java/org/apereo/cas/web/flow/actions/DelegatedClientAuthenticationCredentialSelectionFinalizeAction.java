package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedClientAuthenticationCredentialSelectionFinalizeAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientAuthenticationCredentialSelectionFinalizeAction extends BaseCasWebflowAction {
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        if (!DelegationWebflowUtils.hasDelegatedClientAuthenticationCandidateProfile(requestContext)) {
            val key = WebUtils.getRequestParameterOrAttribute(requestContext, "key").orElseThrow();
            val candidates = DelegationWebflowUtils.getDelegatedClientAuthenticationResolvedCredentials(requestContext, DelegatedAuthenticationCandidateProfile.class);
            val credential = candidates.stream().filter(candidate -> candidate.getKey().equals(key))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unable to locate selected profile for " + key));
            DelegationWebflowUtils.putDelegatedClientAuthenticationCandidateProfile(requestContext, credential);
        }
        return success();
    }
}
