package org.jasig.cas.web.flow.token;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.TokenConstants;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationContextBuilder;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationContextBuilder;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.handler.support.TokenCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * This is {@link TokenAuthenticationAction}.  This class represents an action in the webflow to retrieve
 * user information from an AES128 encrypted token. If the auth_token
 * parameter exists in the web request, it is used to create a new TokenCredential.
 *
 * @author Eric Pierce
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("tokenAuthenticationAction")
public final class TokenAuthenticationAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationAction.class);

    @NotNull
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        final String authTokenValue = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        final Service service =  WebUtils.getService(context);

        if (StringUtils.isNotBlank(authTokenValue) && service != null) {
            try {
                final Credential credential = new TokenCredential(authTokenValue, service);
                LOGGER.debug("Received token authentication request {} ", credential);

                final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                        this.authenticationSystemSupport.getPrincipalElectionStrategy());
                final AuthenticationTransaction transaction =
                        AuthenticationTransaction.wrap(credential);
                this.authenticationSystemSupport.getAuthenticationTransactionManager().handle(transaction,  builder);
                final AuthenticationContext authenticationContext = builder.build(service);

                final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationContext);
                WebUtils.putTicketGrantingTicketInScopes(context, tgt);
                return success();
            } catch (final Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        return error();
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
