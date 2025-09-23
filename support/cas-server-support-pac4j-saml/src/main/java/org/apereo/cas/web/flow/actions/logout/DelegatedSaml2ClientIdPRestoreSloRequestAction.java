package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.support.saml.idp.slo.SamlIdPProfileSingleLogoutRequestProcessor;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedSaml2ClientIdPRestoreSloRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DelegatedSaml2ClientIdPRestoreSloRequestAction extends BaseCasWebflowAction {
    private final SamlIdPProfileSingleLogoutRequestProcessor delegatedSaml2IdPSloRequestProcessor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        delegatedSaml2IdPSloRequestProcessor.restore(requestContext);
        return null;
    }
}
