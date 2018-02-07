package org.apereo.cas.support.pac4j.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.core.profile.definition.ProfileDefinition;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import lombok.extern.slf4j.Slf4j;


/**
 * The purpose of this action is to prepare the PAC4J Profile Manager for Single Logout.
 * 
 * The Profile Manager keeps the profiles in request + session but the session has already been destroyed. This action should restore the
 * profile from an existing authentication (inside the current TGT) and populate the PAC4J Profile Manager with it.
 * 
 * This action should be called from the Logout web flow.
 * 
 * @author jkacer
 * 
 * @since 5.3.0
 */
@Slf4j
public class SingleLogoutPreparationAction extends AbstractAction {

    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final ProfileDefinition<CommonProfile> profileDefinition = new CommonProfileDefinition<>();


    public SingleLogoutPreparationAction(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
            final TicketRegistrySupport ticketRegistrySupport) {
        super();
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
    }


    @Override
    protected Event doExecute(final RequestContext rc) throws Exception {
        String tgtId = WebUtils.getTicketGrantingTicketId(rc);
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(rc);
        if (tgtId == null) {
            tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }

        final Authentication authentication = this.ticketRegistrySupport.getAuthenticationFrom(tgtId);
        final Principal principal = (authentication == null) ? null : authentication.getPrincipal();
        final CommonProfile profile = (principal == null) ? null : ProfileHelper.restoreOrBuildProfile(profileDefinition, principal.getId(),
                authentication.getAttributes());

        if (profile != null) {
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(rc);
            final WebContext webContext = new J2EContext(request, response);
            final ProfileManager pm = Pac4jUtils.getPac4jProfileManager(webContext);
            pm.save(true, profile, false);
            LOGGER.debug("User profile saved in PAC4J Profile Manager.");
        } else {
            LOGGER.debug("No user profile could be restored. SAML Single Logout may not work properly."
                    + " This is normal for non-SAML clients.");
        }

        return success();
    }


}
