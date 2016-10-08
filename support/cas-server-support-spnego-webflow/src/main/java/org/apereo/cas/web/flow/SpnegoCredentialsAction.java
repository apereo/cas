package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.apereo.cas.support.spnego.util.SpnegoConstants;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;
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

    private boolean ntlm;

    private String messageBeginPrefix = constructMessagePrefix();

    /**
     * Behavior in case of SPNEGO authentication failure :
     * <ul><li>True : if SPNEGO is the last authentication method with no fallback.</li>
     * <li>False : if an interactive view (eg: login page) should be send to user as SPNEGO failure fallback</li>
     * </ul>
     */
    private boolean send401OnAuthenticationFailure = true;

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        final String authorizationHeader = request.getHeader(SpnegoConstants.HEADER_AUTHORIZATION);
        logger.debug("SPNEGO Authorization header located as {}", authorizationHeader);
                
        if (StringUtils.hasText(authorizationHeader)
                && authorizationHeader.startsWith(this.messageBeginPrefix)
                && authorizationHeader.length() > this.messageBeginPrefix.length()) {

            logger.debug("SPNEGO Authorization header found with {} bytes",
                    authorizationHeader.length() - this.messageBeginPrefix.length());

            final byte[] token = EncodingUtils.decodeBase64(authorizationHeader.substring(this.messageBeginPrefix.length()));
            if (token == null) {
                logger.warn("Could not decode authorization header in Base64");
                return null;
            }
            logger.debug("Obtained token: {}. Creating SPNEGO credential...", new String(token, Charset.defaultCharset()));
            return new SpnegoCredential(token);
        }

        logger.warn("SPNEGO Authorization header not found under {} or it does not begin with the prefix {}",
                SpnegoConstants.HEADER_AUTHORIZATION, this.messageBeginPrefix);
        return null;
    }

    /**
     * Construct message prefix.
     *
     * @return the string
     */
    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE) + ' ';
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
            logger.debug("No credential was provided. No response header set.");
            return;
        }

        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final SpnegoCredential spnegoCredentials = (SpnegoCredential) credential;
        final byte[] nextToken = spnegoCredentials.getNextToken();
        if (nextToken != null) {
            logger.debug("Obtained output token: {}", new String(nextToken, Charset.defaultCharset()));
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, (this.ntlm
                    ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE)
                    + ' ' + EncodingUtils.encodeBase64(nextToken));
        } else {
            logger.debug("Unable to obtain the output token required.");
        }

        if (spnegoCredentials.getPrincipal() == null && this.send401OnAuthenticationFailure) {
            logger.debug("Setting HTTP Status to 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Sets the ntlm and generate message prefix.
     *
     * @param ntlm the new ntlm
     */
    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
        this.messageBeginPrefix = constructMessagePrefix();
    }

    public void setSend401OnAuthenticationFailure(final boolean send401OnAuthenticationFailure) {
        this.send401OnAuthenticationFailure = send401OnAuthenticationFailure;
    }

}
