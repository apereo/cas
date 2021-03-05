package org.apereo.cas.web.flow;

import org.apereo.cas.support.spnego.util.SpnegoConstants;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * First action of a SPNEGO flow : negotiation.
 * <p>The server checks if the
 * negotiation string is in the request header and this is a supported browser:
 * <ul>
 * <li>If found do nothing and return {@code success()}</li>
 * <li>else add a WWW-Authenticate response header and a 401 response status,
 * then return {@code success()}</li>
 * </ul>
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @author John Gasper
 * @see <a href="http://ietfreport.isoc.org/idref/rfc4559/#page-2">RFC 4559</a>
 * @since 3.1
 */
@Slf4j
@RequiredArgsConstructor
public class SpnegoNegotiateCredentialsAction extends AbstractAction {
    /**
     * Sets supported browsers by their user agent. The user agent
     * header defined will be compared against this list. The user agents configured
     * here need not be an exact match. So longer is the user agent identifier
     * configured in this list is "found" in the user agent header retrieved,
     * the check will pass.
     */
    private final List<String> supportedBrowser;

    /**
     * Whether this is using the NTLM protocol or not.
     */
    private final boolean ntlm;

    /**
     * Sets whether mixed mode authentication should be enabled. If it is
     * enabled then control is allowed to pass back to the Spring Webflow
     * instead of immediately terminating the page after issuing the
     * unauthorized (401) header. This has the effect of displaying the login
     * page on unsupported/configured browsers.
     * <p>
     * If this is set to false then the page is immediately closed after the
     * unauthorized header is sent. This is ideal in environments that only
     * want to use Windows Integrated Auth/SPNEGO and not forms auth.
     */
    private final boolean mixedModeAuthentication;

    @Override
    protected Event doExecute(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        val authorizationHeader = request.getHeader(SpnegoConstants.HEADER_AUTHORIZATION);
        val userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);

        LOGGER.debug("Authorization header [{}], User Agent header [{}]", authorizationHeader, userAgent);
        if (!StringUtils.hasText(userAgent) || this.supportedBrowser.isEmpty()) {
            LOGGER.warn("User Agent header [{}] is empty, or no browsers are supported", userAgent);
            return error();
        }

        if (!isSupportedBrowser(userAgent)) {
            LOGGER.warn("User Agent header [{}] is not supported in the list of supported browsers [{}]",
                userAgent, this.supportedBrowser);
            return error();
        }

        val prefix = constructMessagePrefix();
        if (!StringUtils.hasText(authorizationHeader)
            || !authorizationHeader.startsWith(prefix)
            || authorizationHeader.length() <= prefix.length()) {

            val wwwHeader = this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE;
            LOGGER.debug("Authorization header not found or does not match the message prefix [{}]. Sending [{}] header [{}]",
                prefix, SpnegoConstants.HEADER_AUTHENTICATE, wwwHeader);
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, wwwHeader);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            /*
             The responseComplete flag tells the pausing view-state not to render the response
             because another object has taken care of it. If mixed mode authentication is allowed
             then responseComplete should not be called so that webflow will display the login page.
              */
            if (!this.mixedModeAuthentication) {
                LOGGER.debug("Mixed-mode authentication is disabled. Executing completion of response");
                context.getExternalContext().recordResponseComplete();
            } else {
                LOGGER.debug("Mixed-mode authentication is enabled");
            }
        }
        return success();
    }

    /**
     * Construct message prefix.
     *
     * @return if {@link #ntlm} is enabled, {@link SpnegoConstants#NTLM}, otherwise
     * {@link SpnegoConstants#NEGOTIATE}. An extra space is appended to the end.
     */
    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE) + ' ';
    }

    /**
     * Checks if is supported browser.
     *
     * @param userAgent the user agent
     * @return true, if  supported browser
     */
    protected boolean isSupportedBrowser(final String userAgent) {
        return supportedBrowser.stream().anyMatch(userAgent::contains);
    }
}
