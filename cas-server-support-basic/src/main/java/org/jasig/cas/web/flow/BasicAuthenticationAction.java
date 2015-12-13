package org.jasig.cas.web.flow;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.UsernamePasswordCredential;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

/**
 * This is {@link BasicAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("basicAuthenticationAction")
public class BasicAuthenticationAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationAction.class);

    private static final String AUTHZ_HEADER = "Authorization";

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);

        final String authHeader = request.getHeader(AUTHZ_HEADER);
        if (authHeader != null) {
            final StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                final String basic = st.nextToken();
                if ("basic".equalsIgnoreCase(basic)) {
                    try {
                        final String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
                        final int p = credentials.indexOf(':');
                        if (p != -1) {
                            final String username = credentials.substring(0, p).trim();
                            final String password = credentials.substring(p + 1).trim();
                            final UsernamePasswordCredential credential = new UsernamePasswordCredential(username, password);
                            LOGGER.debug("Received basic authentication request from credentials {} ", credential);
                            final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(credential);
                            WebUtils.putTicketGrantingTicketInScopes(requestContext, tgt);
                            return success();
                        } else {
                            unauthorized(response, "Invalid authentication credentials.");
                        }
                    } catch (final UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                } else {
                    LOGGER.warn("{} header does not specify a basic authentication request. Skipping basic authentication challenge",
                            AUTHZ_HEADER);
                }
            } else {
                LOGGER.warn("{} header is malformed. Skipping basic authentication challenge", AUTHZ_HEADER);
            }
        } else {
            LOGGER.info("No {} header specified. Sending unauthorized challenge for credentials", AUTHZ_HEADER);
            unauthorized(response);
        }
        return error();
    }

    private void unauthorized(final HttpServletResponse response, final String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"CAS\"");
        response.sendError(HttpStatus.SC_UNAUTHORIZED, message);
    }

    private void unauthorized(final HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }
}
