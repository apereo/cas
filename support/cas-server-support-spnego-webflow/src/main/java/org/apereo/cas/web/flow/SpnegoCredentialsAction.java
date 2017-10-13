package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.apereo.cas.support.spnego.util.SpnegoConstants;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

/**
 * Second action of a SPNEGO flow : decode the gssapi-data and build a new
 * {@link SpnegoCredential}.
 * <p>
 * Once AbstractNonInteractiveCredentialsAction has executed the authentication
 * procedure, this action check whether a principal is present in Credential and
 * add corresponding response headers.</p>
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @see <a href="http://ietfreport.isoc.org/idref/rfc4559/#page-2">RFC 4559</a>
 * @since 3.1
 */
public class SpnegoCredentialsAction extends AbstractNonInteractiveCredentialsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpnegoCredentialsAction.class);
    private final boolean ntlm;
    private final String messageBeginPrefix;

    /**
     * Behavior in case of SPNEGO authentication failure :
     * <ul><li>True : if SPNEGO is the last authentication method with no fallback.</li>
     * <li>False : if an interactive view (eg: login page) should be send to user as SPNEGO failure fallback</li>
     * </ul>
     */
    private boolean send401OnAuthenticationFailure = true;

    public SpnegoCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                   final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                   final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy, final boolean ntlm,
                                   final boolean send401OnAuthenticationFailure) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.ntlm = ntlm;
        this.messageBeginPrefix = (ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE) + ' ';
        this.send401OnAuthenticationFailure = send401OnAuthenticationFailure;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        final String authorizationHeader = request.getHeader(SpnegoConstants.HEADER_AUTHORIZATION);
        LOGGER.debug("SPNEGO Authorization header located as [{}]", authorizationHeader);
                
        if (StringUtils.hasText(authorizationHeader)
                && authorizationHeader.startsWith(this.messageBeginPrefix)
                && authorizationHeader.length() > this.messageBeginPrefix.length()) {

            LOGGER.debug("SPNEGO Authorization header found with [{}] bytes",
                    authorizationHeader.length() - this.messageBeginPrefix.length());

            final byte[] token = EncodingUtils.decodeBase64(authorizationHeader.substring(this.messageBeginPrefix.length()));
            if (token == null) {
                LOGGER.warn("Could not decode authorization header in Base64");
                return null;
            }
            LOGGER.debug("Obtained token: [{}]. Creating SPNEGO credential...", new String(token, Charset.defaultCharset()));
            return new SpnegoCredential(token);
        }

        LOGGER.warn("SPNEGO Authorization header not found under [{}] or it does not begin with the prefix [{}]",
                SpnegoConstants.HEADER_AUTHORIZATION, this.messageBeginPrefix);
        return null;
    }

    @Override
    protected void onError(final RequestContext context) {
        setResponseHeader(context);
    }

    @Override
    protected void onSuccess(final RequestContext context) {
        setResponseHeader(context);
    }

    /**
     * Sets the response header based on the retrieved token.
     *
     * @param context    the context
     */
    private void setResponseHeader(final RequestContext context) {
        final Credential credential = WebUtils.getCredential(context);
        
        if (credential == null) {
            LOGGER.debug("No credential was provided. No response header set.");
            return;
        }

        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        final SpnegoCredential spnegoCredentials = (SpnegoCredential) credential;
        final byte[] nextToken = spnegoCredentials.getNextToken();
        if (nextToken != null) {
            LOGGER.debug("Obtained output token: [{}]", new String(nextToken, Charset.defaultCharset()));
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, (this.ntlm
                    ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
                    + ' ' + EncodingUtils.encodeBase64(nextToken));
        } else {
            LOGGER.debug("Unable to obtain the output token required.");
        }

        if (spnegoCredentials.getPrincipal() == null && this.send401OnAuthenticationFailure) {
            LOGGER.debug("Setting HTTP Status to 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
