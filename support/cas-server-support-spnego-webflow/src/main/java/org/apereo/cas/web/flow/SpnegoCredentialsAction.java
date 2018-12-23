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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

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
@Slf4j
public class SpnegoCredentialsAction extends AbstractNonInteractiveCredentialsAction {

    private final boolean ntlm;
    private final String messageBeginPrefix;

    /**
     * Behavior in case of SPNEGO authentication failure :
     * <ul><li>True : if SPNEGO is the last authentication method with no fallback.</li>
     * <li>False : if an interactive view (eg: login page) should be send to user as SPNEGO failure fallback</li>
     * </ul>
     */
    private final boolean send401OnAuthenticationFailure;

    public SpnegoCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                   final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                   final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                   final boolean ntlm,
                                   final boolean send401OnAuthenticationFailure) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.ntlm = ntlm;
        this.messageBeginPrefix = (ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE) + ' ';
        this.send401OnAuthenticationFailure = send401OnAuthenticationFailure;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val authorizationHeader = request.getHeader(SpnegoConstants.HEADER_AUTHORIZATION);
        LOGGER.debug("SPNEGO Authorization header located as [{}]", authorizationHeader);

        if (StringUtils.isBlank(authorizationHeader)) {
            LOGGER.warn("SPNEGO Authorization header is not found under [{}]", SpnegoConstants.HEADER_AUTHORIZATION);
            return null;
        }

        val authzHeaderLength = authorizationHeader.length();
        val prefixLength = this.messageBeginPrefix.length();
        if (authzHeaderLength > prefixLength && authorizationHeader.startsWith(this.messageBeginPrefix)) {
            LOGGER.debug("SPNEGO Authorization header found with [{}] bytes", authzHeaderLength - prefixLength);
            val base64 = authorizationHeader.substring(prefixLength);
            val token = EncodingUtils.decodeBase64(base64);
            if (token == null) {
                LOGGER.warn("Could not decode authorization header in Base64");
                return null;
            }
            val tokenString = new String(token, Charset.defaultCharset());
            LOGGER.debug("Obtained token: [{}]. Creating credential...", tokenString);
            return new SpnegoCredential(token);
        }

        LOGGER.warn("SPNEGO Authorization header [{}] does not begin with the prefix [{}]", authorizationHeader, messageBeginPrefix);
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
     * @param context the context
     */
    private void setResponseHeader(final RequestContext context) {
        val credential = WebUtils.getCredential(context);

        if (credential == null) {
            LOGGER.debug("No credential was provided. No response header set.");
            return;
        }

        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val spnegoCredentials = (SpnegoCredential) credential;
        val nextToken = spnegoCredentials.getNextToken();
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
