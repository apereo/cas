package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.response.AbstractInweboResponse;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboLoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.InweboPushAuthenticateResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.HttpURLConnection;
import java.net.URL;

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
    protected static void retrieveDeviceName(final JsonNode json,
                                             final InweboDeviceNameResponse response) {
        if (response.isOk()) {
            val name = json.get("name");
            if (name != null) {
                response.setDeviceName(name.asText());
            }
        }
    }

    /**
     * Login search.
     *
     * @param login the login
     * @return the inwebo login search response
     */
    public InweboLoginSearchResponse loginSearch(final String login) {
        val soap = consoleAdmin.loginSearch(login);
        val err = soap.getErr();
        val response = (InweboLoginSearchResponse) buildResponse(new InweboLoginSearchResponse(),
            "loginSearch(" + login + ')', err);
        if (response.isOk()) {
            val count = soap.getCount();
            response.setCount(count);
            if (count == 1) {
                response.setUserId(soap.getId().get(0));
                response.setUserStatus(soap.getStatus().get(0));
                response.setActivationStatus(soap.getActivationStatus().get(0));
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
    }

    /**
     * Check push result for inwebo device.
     *
     * @param login     the login
     * @param sessionId the session id
     * @return the inwebo device name response
     */
    public InweboDeviceNameResponse checkPushResult(final String login, final String sessionId) {
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
    }

    /**
     * Extend the authentication attempt.
     *
     * @param login the login
     * @param token the token
     * @return the inwebo device name response
     */
    public InweboDeviceNameResponse authenticateExtended(final String login, final String token) {
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
    }

    /**
     * Call url.
     *
     * @param url the url
     * @return the json node
     */
    @SneakyThrows
    protected JsonNode call(final String url) {
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
    protected AbstractInweboResponse buildResponse(final AbstractInweboResponse response, final String operation, final String err) {
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
