package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AccepttoMultifactorFetchChannelAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoMultifactorFetchChannelAction extends AbstractAction {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CasConfigurationProperties casProperties;
    private final SessionStore<J2EContext> sessionStore;

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new J2EContext(request, response, this.sessionStore);

        val channel = authenticateAndFetchChannel(requestContext);
        LOGGER.debug("Storing channel [{}] in session", channel);
        AccepttoWebflowUtils.storeChannel(channel, webContext);

        val authentication = WebUtils.getInProgressAuthentication();
        AccepttoWebflowUtils.storeAuthentication(authentication, webContext);

        val accepttoRedirectUrl = buildAccepttoAuthenticationSelectionUrl(request, channel);
        LOGGER.debug("Redirecting to [{}]", accepttoRedirectUrl);
        requestContext.getRequestScope().put("accepttoRedirectUrl", accepttoRedirectUrl);
        return new EventFactorySupport().success(this);
    }

    /**
     * Build acceptto authentication selection url string.
     *
     * @param request the request
     * @param channel the channel
     * @return the string
     * @throws Exception the exception
     */
    protected String buildAccepttoAuthenticationSelectionUrl(final HttpServletRequest request, final String channel) throws Exception {
        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val callbackUrl = WebUtils.getHttpRequestFullUrl(request);
        return new URIBuilder(acceptto.getAuthnSelectionUrl() + "/mfa/index")
            .addParameter("channel", channel)
            .addParameter("callback_url", callbackUrl)
            .build()
            .toString();
    }

    /**
     * Authenticate and fetch channel.
     *
     * @param requestContext the request
     * @return the channel
     */
    protected String authenticateAndFetchChannel(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val url = StringUtils.appendIfMissing(acceptto.getApiUrl(), "/") + "authenticate_with_options";

        LOGGER.trace("Contacting API [{}] to fetch channel", url);

        val authentication = WebUtils.getInProgressAuthentication();
        val attributes = authentication.getPrincipal().getAttributes();
        LOGGER.debug("Current principal attributes are [{}]", attributes);

        val email = CollectionUtils.firstElement(attributes.get(acceptto.getEmailAttribute()))
            .map(Object::toString)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine user email address"));

        LOGGER.debug("Principal email address determined from attribute [{}] is [{}]", acceptto.getEmailAttribute(), email);
        val parameters = CollectionUtils.<String, Object>wrap(
            "uid", acceptto.getApplicationId(),
            "secret", acceptto.getSecret(),
            "email", email,
            "message", acceptto.getMessage(),
            "timeout", acceptto.getTimeout());

        CookieUtils.getCookieFromRequest("jwt", request)
            .ifPresent(cookie -> parameters.put("jwt", cookie.getValue()));

        val currentCredential = WebUtils.getCredential(requestContext);
        if (currentCredential instanceof AccepttoEmailCredential) {
            parameters.put("auth_type", 1);
        }
        
        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(url, parameters, new HashMap<>(0));
            if (response != null) {
                val status = response.getStatusLine().getStatusCode();
                LOGGER.debug("Response status code is [{}]", status);

                if (status == HttpStatus.SC_OK) {
                    val results = MAPPER.readValue(response.getEntity().getContent(), Map.class);
                    LOGGER.debug("Received API results as [{}]", results);

                    val channel = results.get("channel").toString();
                    val channelStatus = (String) results.get("status");
                    if (StringUtils.isNotBlank(channelStatus)
                        && ("denied".equalsIgnoreCase(channelStatus) || "rejected".equalsIgnoreCase(channelStatus))) {
                        throw new AuthenticationException("Authentication attempt has been denied");
                    }
                    return channel;
                }
                if (status == HttpStatus.SC_FORBIDDEN) {
                    throw new AuthenticationException("Invalid uid and secret combination; application not found");
                }
                if (status == HttpStatus.SC_UNAUTHORIZED) {
                    throw new AuthenticationException("Email address provided is not a valid registered account");
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        throw new IllegalArgumentException("Unable to fetch channel for user");
    }
}
