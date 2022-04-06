package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccountProfilePasswordChangeRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class AccountProfilePasswordChangeRequestAction extends BaseCasWebflowAction {
    private final CentralAuthenticationService centralAuthenticationService;

    private final PasswordResetUrlBuilder passwordResetUrlBuilder;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicket(requestContext);
        try {
            val principal = tgt.getAuthentication().getPrincipal();
            val url = passwordResetUrlBuilder.build(principal.getId()).toExternalForm();
            LOGGER.debug("Redirecting password reset flow to [{}]", url);
            WebUtils.putServiceRedirectUrl(requestContext, url);
            return success(url);
        } finally {
            centralAuthenticationService.deleteTicket(tgt);
        }
    }
}
