package org.apereo.cas.web.flow.token;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.TokenConstants;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.handler.support.TokenCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link TokenAuthenticationAction}.  This class represents an action in the webflow to retrieve
 * user information from an AES128 encrypted token. If the auth_token
 * parameter exists in the web request, it is used to create a new TokenCredential.
 *
 * @author Eric Pierce
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RefreshScope
@Component("tokenAuthenticationAction")
public class TokenAuthenticationAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationAction.class);

    
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    
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

                final AuthenticationResult authenticationResult =
                        this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

                final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
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
