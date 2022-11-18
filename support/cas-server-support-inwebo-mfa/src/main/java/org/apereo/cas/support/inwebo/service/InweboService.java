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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apereo.cas.support.inwebo.web.flow.actions.WebflowConstants.*;

/**
 * The Inwebo service.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Slf4j
public record InweboService(CasConfigurationProperties casProperties, InweboConsoleAdmin consoleAdmin, SSLContext context) {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

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
                var activationStatus = loginSearchResult.getActivationStatus().get(0);
                val userId = loginSearchResult.getId().get(0);
                if (activationStatus == 1) {
                    val loginQueryResult = consoleAdmin.loginQuery(userId);
                    if ("OK".equals(loginQueryResult.getErr())) {
                        var hasAuthenticator = false;
                        for (val maname : loginQueryResult.getManame()) {
                            if (maname.contains("Authenticator")) {
                                hasAuthenticator = true;
                                break;
                            }
                        }
                        if (!hasAuthenticator) {
                            activationStatus = BROWSER_AUTHENTICATION_STATUS;
                        } else if (loginQueryResult.getManame().size() > 2) {
                            activationStatus = PUSH_AND_BROWSER_AUTHENTICATION_STATUS;
                        }
                    }
                }
                response.setUserId(userId);
                response.setUserStatus(loginSearchResult.getStatus().get(0));
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
            val url = UriComponentsBuilder.fromHttpUrl(inwebo.getServiceApiUrl())
                .queryParam("action", "pushAuthenticate")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("format", "json")
                .toUriString();

            val json = call(url);
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
            val url = UriComponentsBuilder.fromHttpUrl(inwebo.getServiceApiUrl())
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
            val url = UriComponentsBuilder.fromHttpUrl(inwebo.getServiceApiUrl())
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
        val conn = (HttpURLConnection) new URL(url).openConnection();
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection.class.cast(conn)
                .setSSLSocketFactory(this.context.getSocketFactory());
        }
        conn.setRequestMethod(HttpMethod.GET.name());
        return MAPPER.readTree(conn.getInputStream());
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

            if ("NOK:NOPUSH".equals(err)) {
                response.setResult(InweboResult.NOPUSH);
            } else if ("NOK:NOMA".equals(err)) {
                response.setResult(InweboResult.NOMA);
            } else if ("NOK:NOLOGIN".equals(err)) {
                response.setResult(InweboResult.NOLOGIN);
            } else if ("NOK:SN".equals(err)) {
                response.setResult(InweboResult.SN);
            } else if ("NOK:srv unknown".equals(err)) {
                response.setResult(InweboResult.UNKNOWN_SERVICE);
            } else if ("NOK:WAITING".equals(err)) {
                response.setResult(InweboResult.WAITING);
            } else if ("NOK:REFUSED".equals(err)) {
                response.setResult(InweboResult.REFUSED);
            } else if ("NOK:TIMEOUT".equals(err)) {
                response.setResult(InweboResult.TIMEOUT);
            } else {
                response.setResult(InweboResult.NOK);
            }
        }
        return response;
    }
}
