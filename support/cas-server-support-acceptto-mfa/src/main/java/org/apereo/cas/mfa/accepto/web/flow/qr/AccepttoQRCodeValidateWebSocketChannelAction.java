package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoWebflowUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.hjson.JsonValue;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AccepttoQRCodeValidateWebSocketChannelAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoQRCodeValidateWebSocketChannelAction extends AbstractAction {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CasConfigurationProperties casProperties;

    private final SessionStore<JEEContext> sessionStore;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response, this.sessionStore);

        val channel = request.getParameter("channel");
        if (channel == null) {
            return returnError("Unable to locate websocket channel");
        }

        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val url = StringUtils.appendIfMissing(acceptto.getApiUrl(), "/") + "get_user_by_websocket_channel";

        LOGGER.trace("Contacting API [{}] to fetch email address", url);
        val parameters = CollectionUtils.<String, Object>wrap(
            "uid", acceptto.getApplicationId(),
            "secret", acceptto.getSecret(),
            "websocket_channel", channel);

        HttpResponse apiResponse = null;
        try {
            apiResponse = HttpUtils.executePost(url, parameters, new HashMap<>(0));
            if (apiResponse != null) {
                val status = apiResponse.getStatusLine().getStatusCode();
                LOGGER.debug("Response API status code is [{}]", status);

                if (status == HttpStatus.SC_OK) {
                    val result = IOUtils.toString(apiResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                    val results = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                    LOGGER.debug("Received API results for channel [{}] as [{}]", channel, results);

                    val success = BooleanUtils.toBoolean(results.get("success").toString());
                    if (success) {
                        val email = results.get("user_email").toString();
                        LOGGER.trace("Storing channel [{}] in http session", channel);
                        AccepttoWebflowUtils.storeChannelInSessionStore(channel, webContext);
                        WebUtils.putCredential(requestContext, new AccepttoEmailCredential(email));
                        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_FINALIZE);
                    }
                    val message = results.get("message").toString();
                    LOGGER.error(message);
                    return returnError(message);
                }
                if (status == HttpStatus.SC_FORBIDDEN) {
                    return returnError("Invalid uid and secret combination; application not found");
                }
                if (status == HttpStatus.SC_UNAUTHORIZED) {
                    return returnError("Email address provided is not a valid registered account");
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return returnError(e.getMessage());
        } finally {
            HttpUtils.close(apiResponse);
        }
        return returnError("Unable to validate websocket channel");
    }

    private Event returnError(final String message) {
        val eventAttributes = new LocalAttributeMap<>();
        LOGGER.error(message);
        eventAttributes.put("error",
            new AuthenticationException(new UnauthorizedAuthenticationException(message)));
        return new EventFactorySupport().event(this,
            CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, eventAttributes);
    }
}
