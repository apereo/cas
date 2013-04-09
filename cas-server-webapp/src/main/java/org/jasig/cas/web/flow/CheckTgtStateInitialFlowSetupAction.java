package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Class to automatically ensure that stored TGT is still valid.
 * <p/>
 * If the system detects that the TGT ticket is invalid then artifacts that hold
 * Single Sign On session will be automatically removed.
 * <p/>
 *
 * @author David Ordas
 * @see InitialFlowSetupAction
 * @see <a href="https://issues.jasig.org/browse/CAS-1219">CAS-1219 issue</a>
 * @since 4.0.0
 */
public final class CheckTgtStateInitialFlowSetupAction extends AbstractAction {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean onlyOnNoService = false;

    /**
     * The CORE to which we delegate for all CAS functionality.
     */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /**
     * TicketRegistry for storing and retrieving tickets as needed.
     */
    @NotNull
    private TicketRegistry ticketRegistry;

    /**
     * CookieGenerator for the Warnings.
     */
    @NotNull
    private CookieRetrievingCookieGenerator warnCookieGenerator;

    /**
     * CookieGenerator for the TicketGrantingTickets.
     */
    @NotNull
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    protected Event doExecute(final RequestContext context) throws Exception {
        final String tgtId = WebUtils.getTicketGrantingTicketId(context);
        final Service service = WebUtils.getService(context);
        if (tgtId != null && (!this.onlyOnNoService || (this.onlyOnNoService && service == null))) {
            log.debug("Checking for inconsistent CAS Single Sign On session used by TGT ticket: {}", tgtId);

            final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
            if (tgt == null || tgt.isExpired()) {
                final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
                final MutableAttributeMap flowScope = context.getFlowScope();
                final MutableAttributeMap requestScope = context.getRequestScope();
                log.info("Destroying detected inconsistent CAS Single Sign On session used by TGT ticket: {}", tgtId);

                //Destroy SSO cookies
                this.ticketGrantingTicketCookieGenerator.removeCookie(response);
                this.warnCookieGenerator.removeCookie(response);

                //Destroy webflow SSO artifacts exposed by the InitialFlowSetupAction
                flowScope.remove("ticketGrantingTicketId");
                flowScope.remove("warnCookieValue");
                requestScope.remove("ticketGrantingTicketId");
                requestScope.remove("warnCookieValue");

                //Destroy this TGT to perform SLO in services used by
                this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);

                //Throw an exception to capture it in a global webflow transition
                throw new InconsistentTgtInitialStateException();
            }
        }
        return success();
    }

    /**
     * @param onlyOnNoService <tt>true</tt> to enable this check only
     *                        when no service can be extracted, <tt>false</tt> otherwise
     */
    public void setOnlyOnNoService(final boolean onlyOnNoService) {
        this.onlyOnNoService = onlyOnNoService;
    }

    public void setCentralAuthenticationService(
            final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setTicketGrantingTicketCookieGenerator(
            final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setWarnCookieGenerator(
            final CookieRetrievingCookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }
}
