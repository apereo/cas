package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.response.AbstractInweboResponse;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboLoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.InweboPushAuthenticateResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.HttpURLConnection;
import java.net.URI;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.BROWSER_AUTHENTICATION_STATUS;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.PUSH_AND_BROWSER_AUTHENTICATION_STATUS;

/**
 * The Inwebo service.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class InweboService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CasConfigurationProperties casProperties;
    private final InweboConsoleAdmin consoleAdmin;
    private final SSLContext context;

    /**
     * Retrieve device name.
     *
     * @param json     the json
     * @param response the response
     */
    static void retrieveDeviceName(final JsonNode json,
                                   final InweboDeviceNameResponse response) {
        if (response.isOk()) {
            val name = json.get("name");
            if (name != null) {
                response.setDeviceName(name.asText());
            }
        }
    }

    /**
     * Login search and query.
     *
     * @param login the login
     * @return the inwebo login search/query response
     */
    public InweboLoginSearchResponse loginSearchQuery(final String login) {
        val loginSearchResult = consoleAdmin.loginSearch(login);
        val err = loginSearchResult.getErr();
        val response = (InweboLoginSearchResponse) buildResponse(new InweboLoginSearchResponse(),
            "loginSearch(" + login + ')', err);
        if (response.isOk()) {
            val count = loginSearchResult.getCount();
            response.setCount(count);
            if (count == 1) {
                var activationStatus = loginSearchResult.getActivationStatus().getFirst();
                val userId = loginSearchResult.getId().getFirst();
                if (activationStatus == 1) {
                    val loginQueryResult = consoleAdmin.loginQuery(userId);
                    if ("OK".equals(loginQueryResult.getErr())) {
                        var hasAuthenticator = loginQueryResult.getManame().stream().anyMatch(maname -> maname.contains("Authenticator"));
                        if (!hasAuthenticator) {
                            activationStatus = BROWSER_AUTHENTICATION_STATUS;
                        } else if (loginQueryResult.getManame().size() > 2) {
                            activationStatus = PUSH_AND_BROWSER_AUTHENTICATION_STATUS;
                        }
                    }
                }
                response.setUserId(userId);
                response.setUserStatus(loginSearchResult.getStatus().getFirst());
                response.setActivationStatus(activationStatus);
            }
        }
        return response;
    }

    /**
     * Push authenticate.
     *
     * @param login the login
     * @return the inwebo push authenticate response
     */
    public InweboPushAuthenticateResponse pushAuthenticate(final String login) {
        return FunctionUtils.doUnchecked(() -> {
            val inwebo = casProperties.getAuthn().getMfa().getInwebo();
            val url = UriComponentsBuilder.fromUriString(inwebo.getServiceApiUrl())
                .queryParam("action", "pushAuthenticate")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("format", "json")
                .toUriString();

            val json = call(url);
            LOGGER.debug("Push authenticate response from [{}]: [{}]", url, json.toPrettyString());
            val err = json.get("err").asText("OK");
            val response = (InweboPushAuthenticateResponse) buildResponse(
                new InweboPushAuthenticateResponse(), "pushAuthenticate(" + login + ')', err);
            if (response.isOk()) {
                val sessionId = json.get("sessionId");
                if (sessionId != null) {
                    response.setSessionId(sessionId.asText());
                }
            }
            return response;
        });
    }

    /**
     * Check push result for inwebo device.
     *
     * @param login     the login
     * @param sessionId the session id
     * @return the inwebo device name response
     */
    public InweboDeviceNameResponse checkPushResult(final String login, final String sessionId) {
        return FunctionUtils.doUnchecked(() -> {
            val inwebo = casProperties.getAuthn().getMfa().getInwebo();
            val url = UriComponentsBuilder.fromUriString(inwebo.getServiceApiUrl())
                .queryParam("action", "checkPushResult")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("sessionId", sessionId)
                .queryParam("format", "json")
                .toUriString();

            val json = call(url);
            val err = json.get("err").asText("OK");
            val response = (InweboDeviceNameResponse) buildResponse(new InweboDeviceNameResponse(),
                "checkPushResult(" + login + ')', err);
            retrieveDeviceName(json, response);
            return response;
        });
    }

    /**
     * Extend the authentication attempt.
     *
     * @param login the login
     * @param token the token
     * @return the inwebo device name response
     */
    public InweboDeviceNameResponse authenticateExtended(final String login, final String token) {
        return FunctionUtils.doUnchecked(() -> {
            val inwebo = casProperties.getAuthn().getMfa().getInwebo();
            val url = UriComponentsBuilder.fromUriString(inwebo.getServiceApiUrl())
                .queryParam("action", "authenticateExtended")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("token", token)
                .queryParam("format", "json")
                .toUriString();

            val json = call(url);
            val err = json.get("err").asText("OK");
            val response = (InweboDeviceNameResponse) buildResponse(
                new InweboDeviceNameResponse(), "authenticateExtended(" + login + ')', err);
            retrieveDeviceName(json, response);
            return response;
        });
    }

    /**
     * Call url.
     *
     * @param url the url
     * @return the json node
     * @throws Exception the exception
     */
    JsonNode call(final String url) throws Exception {
        val conn = (HttpURLConnection) new URI(url).toURL().openConnection();
        if (conn instanceof final HttpsURLConnection urlConnection) {
            urlConnection.setSSLSocketFactory(this.context.getSocketFactory());
        }
        conn.setRequestMethod(HttpMethod.GET.name());
        try (var input = conn.getInputStream()) {
            return MAPPER.readTree(input);
        }
    }

    /**
     * Build response.
     *
     * @param response  the response
     * @param operation the operation
     * @param err       the err
     * @return the abstract inwebo response
     */
    AbstractInweboResponse buildResponse(final AbstractInweboResponse response, final String operation, final String err) {
        if ("OK".equals(err)) {
            response.setResult(InweboResult.OK);
        } else {
            LOGGER.trace("Inwebo call: [{}] returned error: [{}]", operation, err);

            switch (err) {
                case "NOK:NOPUSH" -> response.setResult(InweboResult.NOPUSH);
                case "NOK:NOMA" -> response.setResult(InweboResult.NOMA);
                case "NOK:NOLOGIN" -> response.setResult(InweboResult.NOLOGIN);
                case "NOK:SN" -> response.setResult(InweboResult.SN);
                case "NOK:srv unknown" -> response.setResult(InweboResult.UNKNOWN_SERVICE);
                case "NOK:WAITING" -> response.setResult(InweboResult.WAITING);
                case "NOK:REFUSED" -> response.setResult(InweboResult.REFUSED);
                case "NOK:TIMEOUT" -> response.setResult(InweboResult.TIMEOUT);
                case null, default -> response.setResult(InweboResult.NOK);
            }
        }
        return response;
    }

}
