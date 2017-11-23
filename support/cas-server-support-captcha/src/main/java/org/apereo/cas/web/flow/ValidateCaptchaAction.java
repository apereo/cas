package org.apereo.cas.web.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * This is {@link ValidateCaptchaAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ValidateCaptchaAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateCaptchaAction.class);
    private static final ObjectReader READER = new ObjectMapper().findAndRegisterModules().reader();
    private static final String CODE = "captchaError";

    private final GoogleRecaptchaProperties recaptchaProperties;

    public ValidateCaptchaAction(final GoogleRecaptchaProperties recaptchaProperties) {
        this.recaptchaProperties = recaptchaProperties;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        if (StringUtils.isBlank(gRecaptchaResponse)) {
            LOGGER.warn("Recaptcha response is missing from the request");
            return getError(requestContext);
        }
        try {
            final URL obj = new URL(recaptchaProperties.getVerifyUrl());
            final HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", WebUtils.getHttpServletRequestUserAgentFromRequestContext());
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            final String postParams = "secret=" + recaptchaProperties.getSecret() + "&response=" + gRecaptchaResponse;

            LOGGER.debug("Sending 'POST' request to URL: [{}]", obj);
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(postParams);
                wr.flush();
            }
            final int responseCode = con.getResponseCode();
            LOGGER.debug("Response Code: [{}]", responseCode);

            if (responseCode == HttpStatus.OK.value()) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    final String response = in.lines().collect(Collectors.joining());
                    LOGGER.debug("Google captcha response received: [{}]", response);
                    final JsonNode node = READER.readTree(response);
                    if (node.has("success") && node.get("success").booleanValue()) {
                        return null;
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return getError(requestContext);
    }

    private Event getError(final RequestContext requestContext) {
        final MessageContext messageContext = requestContext.getMessageContext();
        messageContext.addMessage(new MessageBuilder().error().code(CODE).build());
        return getEventFactorySupport().event(this, CODE);
    }
}
