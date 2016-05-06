package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credential such as client certificates, NTLM, etc.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
@RefreshScope
@Component
public abstract class AbstractNonInteractiveCredentialsAction extends AbstractAction {

    /** The logger instance. */
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Principal factory instance. */
    @Autowired
    @Qualifier("principalFactory")
    protected PrincipalFactory principalFactory;

    
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    /** Instance of warn cookie generator. */
    @Autowired(required=false)
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    /**
     * Checks if is renew present.
     *
     * @param context the context
     * @return true, if  renew present
     */
    protected boolean isRenewPresent(final RequestContext context) {
        return StringUtils.hasText(context.getRequestParameters().get(CasProtocolConstants.PARAMETER_RENEW));
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final Credential credential = constructCredentialsFromRequest(context);

        if (credential == null) {
            return error();
        }

        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final Service service = WebUtils.getService(context);

        if (isRenewPresent(context) && ticketGrantingTicketId != null && service != null) {

            try {
                final AuthenticationResult authenticationResult =
                        this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

                final ServiceTicket serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId, service, authenticationResult);
                WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
                onWarn(context, credential);
                return result("warn");

            } catch (final AuthenticationException e) {
                onError(context, credential);
                return error();
            } catch (final AbstractTicketException e) {
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
                logger.debug("Attempted to generate a ServiceTicket using renew=true with different credential", e);
            }
        }

        try {
            final AuthenticationResult authenticationResult =
                    this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

            final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            onSuccess(context, credential);
            return success();

        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
            onError(context, credential);
            return error();
        }
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }


    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning an error event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onError(final RequestContext context, final Credential credential) {
        // default implementation does nothing
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning a success event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onSuccess(final RequestContext context, final Credential credential) {
        // default implementation does nothing
    }

    public PrincipalFactory getPrincipalFactory() {
        return this.principalFactory;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return this.authenticationSystemSupport;
    }

    /**
     * Hook method to note to the flow thar a warning
     * must be issued prior to resuming the normal
     * authentication flow.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onWarn(final RequestContext context, final Credential credential) {
        WebUtils.putWarnCookieIfRequestParameterPresent(this.warnCookieGenerator, context);
    }

    /**
     * Abstract method to implement to construct the credential from the
     * request object.
     *
     * @param context the context for this request.
     * @return the constructed credential or null if none could be constructed
     * from the request.
     */
    protected abstract Credential constructCredentialsFromRequest(RequestContext context);
}
