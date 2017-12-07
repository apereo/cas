package org.apereo.cas.support.pac4j.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;


/**
 * The purpose of this action is to prepare the PAC4J Profile Manager for Single Logout.
 * 
 * The Profile Manager keeps the profiles in request + session but the session has already been destroyed. This action should restore the
 * profile from a long term storage - {@link ProfileService} and populate the PAC4J Profile Manager with it.
 * 
 * This action should be called from the Logout web flow.
 * 
 * @author jkacer
 * 
 * @since 5.3.0
 */
public class SingleLogoutPreparationAction extends AbstractAction {

    private static final Logger LOGGER2 = LoggerFactory.getLogger(SingleLogoutPreparationAction.class);

    private final ProfileService<? extends CommonProfile> profileService;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;


    public SingleLogoutPreparationAction(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
            final ProfileService<? extends CommonProfile> profileService) {
        super();
        this.profileService = profileService;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Event doExecute(final RequestContext rc) throws Exception {
        // Get the TGT first. For logout, we need to get the cookie's value, most likely the TGT will not be in the scope anymore.
        String tgtId = WebUtils.getTicketGrantingTicketId(rc);
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(rc);
        if (tgtId == null) {
            tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }

        // Retrieve the user profile previously stored to the long-term storage in PAC4J DelegatedClientAuthenticationAction.
        final CommonProfile profile = (tgtId == null) ? null : profileService.findByLinkedId(tgtId);

        // And save the profile into the PAC4J Profile Manager for this request + session.
        if (profile != null) {
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(rc);
            final WebContext webContext = new J2EContext(request, response);
            final ProfileManager pm = Pac4jUtils.getPac4jProfileManager(webContext);
            pm.save(true, profile, false);
            profileService.removeById(profile.getId());
            LOGGER2.debug("User profile restored from a long-term storage and saved in PAC4J Profile Manager.");
        } else {
            LOGGER2.debug("No user profile restored from a long-term storage. SAML Single Logout may not work properly."
                    + " This is normal for non-SAML clients.");
        }

        return success();
    }


}
