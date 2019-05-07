package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
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
    /**
     * Session attribute to hold the authentication channel.
     */
    public static final String SESSION_ATTRIBUTE_CHANNEL = "acceptoMfaChannel";

    /**
     * Session attribute to hold original authn.
     */
    public static final String SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION = "acceptoMfaOriginalAuthN";

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CasConfigurationProperties casProperties;
    private final SessionStore<J2EContext> sessionStore;

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new J2EContext(request, response, this.sessionStore);

        val channel = authenticateAndFetchChannel();
        LOGGER.debug("Storing channel [{}] in http session", channel);
        webContext.getSessionStore().set(webContext, SESSION_ATTRIBUTE_CHANNEL, channel);

        val authentication = WebUtils.getInProgressAuthentication();
        webContext.getSessionStore().set(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION, authentication);

        val callbackUrl = WebUtils.getHttpRequestFullUrl(request);
        val accepttoRedirectUrl = new URIBuilder(acceptto.getAuthnSelectionUrl() + "/mfa/index")
            .addParameter("channel", channel)
            .addParameter("callback_url", callbackUrl)
            .build()
            .toString();

        LOGGER.debug("Redirecting to [{}]", accepttoRedirectUrl);
        requestContext.getRequestScope().put("accepttoRedirectUrl", accepttoRedirectUrl);
        return new EventFactorySupport().success(this);
    }

    /**
     * Authenticate and fetch channel.
     *
     * @return the channel
     */
    protected String authenticateAndFetchChannel() {
        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val url = StringUtils.appendIfMissing(acceptto.getApiUrl(), "/") + "authenticate";

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
