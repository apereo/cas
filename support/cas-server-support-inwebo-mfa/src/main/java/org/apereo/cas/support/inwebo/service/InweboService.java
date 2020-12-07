package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.response.AbstractInweboResponse;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboLoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.InweboPushAuthenticateResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ssl.SSLUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;

/**
 * The Inwebo service.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Slf4j
public class InweboService {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final String API_URL = "https://api.myinwebo.com/FS?";

    private final CasConfigurationProperties casProperties;

    private final InweboConsoleAdmin consoleAdmin;

    private SSLContext context;

    public InweboService(final CasConfigurationProperties casProperties, final InweboConsoleAdmin consoleAdmin) {
        this.casProperties = casProperties;
        this.consoleAdmin = consoleAdmin;

        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        try {
            val keyManagerFactory = SSLUtils.buildKeystore(inwebo.getClientCertificate());
            this.context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), null, RandomUtils.getNativeInstance());
        } catch (final Exception e) {
            throw new RuntimeException("Cannot initialize Inwebo service", e);
        }
    }

    public InweboLoginSearchResponse loginSearch(final String login) {
        val soap = consoleAdmin.loginSearch(login, casProperties.getAuthn().getMfa().getInwebo().getServiceId());
        val err = soap.getErr();
        val response = (InweboLoginSearchResponse) buildResponse(new InweboLoginSearchResponse(), "loginSearch(" + login + ")", err);
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

    public InweboPushAuthenticateResponse pushAuthenticate(final String login) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("action", "pushAuthenticate")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("format", "json")
                .toUriString();

        val json = call(url);
        val err = json.get("err").asText("OK");
        val response = (InweboPushAuthenticateResponse) buildResponse(new InweboPushAuthenticateResponse(), "pushAuthenticate(" + login + ")", err);
        if (response.isOk()) {
            val sessionId = json.get("sessionId");
            if (sessionId != null) {
                response.setSessionId(sessionId.asText());
            }
        }
        return response;
    }

    public InweboDeviceNameResponse checkPushResult(final String login, final String sessionId) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("action", "checkPushResult")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("sessionId", sessionId)
                .queryParam("format", "json")
                .toUriString();

        val json = call(url);
        val err = json.get("err").asText("OK");
        val response = (InweboDeviceNameResponse) buildResponse(new InweboDeviceNameResponse(), "checkPushResult(" + login + ")", err);
        retrieveDeviceName(json, response);
        return response;
    }

    protected void retrieveDeviceName(final JsonNode json, final InweboDeviceNameResponse response) {
        if (response.isOk()) {
            val name = json.get("name");
            if (name != null) {
                response.setDeviceName(name.asText());
            }
        }
    }

    public InweboDeviceNameResponse authenticateExtended(final String login, final String token) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("action", "authenticateExtended")
                .queryParam("serviceId", inwebo.getServiceId())
                .queryParam("userId", login)
                .queryParam("token", token)
                .queryParam("format", "json")
                .toUriString();

        val json = call(url);
        val err = json.get("err").asText("OK");
        val response = (InweboDeviceNameResponse) buildResponse(new InweboDeviceNameResponse(), "authenticateExtended(" + login + ")", err);
        retrieveDeviceName(json, response);
        return response;
    }

    protected JsonNode call(final String url) {
        try {
            val conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setSSLSocketFactory(this.context.getSocketFactory());

            conn.setRequestMethod("GET");
            return MAPPER.readTree(conn.getInputStream());
        } catch (final IOException e) {
            throw new RuntimeException("Inwebo call failed: " + url, e);
        }
    }

    protected AbstractInweboResponse buildResponse(final AbstractInweboResponse response, final String operation, final String err) {
        if ("OK".equals(err)) {
            response.setResult(InweboResult.OK);
        } else {
            LOGGER.debug("Inwebo call: {} returned error: {}", operation, err);
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
