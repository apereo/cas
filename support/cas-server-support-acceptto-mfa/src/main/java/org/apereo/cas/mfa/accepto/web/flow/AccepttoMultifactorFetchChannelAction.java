package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoApiUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.security.PublicKey;

/**
 * This is {@link AccepttoMultifactorFetchChannelAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoMultifactorFetchChannelAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;
    private final SessionStore<J2EContext> sessionStore;
    private final PublicKey apiPublicKey;

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new J2EContext(request, response, this.sessionStore);

        val channel = authenticateAndFetchChannel(requestContext);
        LOGGER.debug("Storing channel [{}] in session", channel);
        AccepttoWebflowUtils.storeChannelInSessionStore(channel, webContext);

        val authentication = WebUtils.getInProgressAuthentication();
        AccepttoWebflowUtils.storeAuthenticationInSessionStore(authentication, webContext);

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
        try {
            val existingChannel = AccepttoWebflowUtils.getChannel(requestContext);
            if (existingChannel.isPresent()) {
                val channel = existingChannel.get();
                LOGGER.debug("Using existing channel retrieved as [{}}", channel);
                return channel;
            }

            val authentication = WebUtils.getInProgressAuthentication();
            val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
            val results = AccepttoApiUtils.authenticate(authentication, acceptto, requestContext, this.apiPublicKey);
            LOGGER.debug("Received API results as [{}]", results);

            if (results.containsKey("channel")) {
                val channel = results.get("channel").toString();
                val channelStatus = (String) results.get("status");
                if (StringUtils.isNotBlank(channelStatus) && ("denied".equalsIgnoreCase(channelStatus) || "rejected".equalsIgnoreCase(channelStatus))) {
                    throw new AuthenticationException("Authentication attempt has been denied");
                }
                return channel;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new AuthenticationException("Unable to fetch channel for user");
    }
}
