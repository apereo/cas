package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AccepttoMultifactorDetermineUserAccountStatusAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoMultifactorDetermineUserAccountStatusAction extends AbstractAction {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CasConfigurationProperties casProperties;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val url = StringUtils.appendIfMissing(acceptto.getApiUrl(), "/") + "is_user_valid";

        LOGGER.trace("Contacting [{}] to fetch user account status", url);

        val authentication = WebUtils.getInProgressAuthentication();
        val attributes = authentication.getPrincipal().getAttributes();
        LOGGER.debug("Current principal attributes are [{}]", attributes);

        val email = CollectionUtils.firstElement(attributes.get(acceptto.getEmailAttribute()))
            .map(Object::toString)
            .orElseThrow(null);

        val eventFactorySupport = new EventFactorySupport();
        if (StringUtils.isBlank(email)) {
            LOGGER.error("Unable to determine email address under attribute [{}]", acceptto.getEmailAttribute());
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        }

        LOGGER.debug("Principal email address determined from attribute [{}] is [{}]", acceptto.getEmailAttribute(), email);
        val parameters = CollectionUtils.<String, Object>wrap(
            "uid", acceptto.getApplicationId(),
            "secret", acceptto.getSecret(),
            "email", email);

        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(url, parameters, new HashMap<>(0));
            if (response != null) {
                val status = response.getStatusLine().getStatusCode();
                LOGGER.debug("Response status code is [{}]", status);

                if (status == HttpStatus.SC_OK) {
                    val results = MAPPER.readValue(response.getEntity().getContent(), Map.class);
                    LOGGER.debug("Received user-account API results as [{}]", results);
                    val valid = results.get("valid").toString();
                    val registrationState = results.get("registration_state").toString();

                    if (!BooleanUtils.toBoolean(valid)) {
                        LOGGER.error("User account [{}] does not have a valid status", email);
                        return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
                    }

                    if (!StringUtils.equalsIgnoreCase("finished", registrationState)) {
                        LOGGER.warn("User account [{}] has not finished the registration process yet", email);
                    }
                } else {
                    if (status == HttpStatus.SC_FORBIDDEN) {
                        LOGGER.error("Invalid application id and secret combination");
                    }
                    if (status == HttpStatus.SC_UNAUTHORIZED) {
                        LOGGER.error("Email address provided is not a valid registered account");
                    }
                    return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }

        return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }
}
