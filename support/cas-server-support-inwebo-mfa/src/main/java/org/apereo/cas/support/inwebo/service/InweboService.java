package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.response.AbstractResponse;
import org.apereo.cas.support.inwebo.service.response.DeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.LoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.PushAuthenticateResponse;
import org.apereo.cas.support.inwebo.service.response.Result;
import org.apereo.cas.util.RandomUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The Inwebo service.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Slf4j
public class InweboService {

    private static final String API_URL = "https://api.myinwebo.com/FS?";

    private final CasConfigurationProperties casProperties;

    private final ConsoleAdmin consoleAdmin;

    private SSLContext context;

    private final ObjectMapper mapper = new ObjectMapper();

    public InweboService(final CasConfigurationProperties casProperties, final ConsoleAdmin consoleAdmin) {
        this.casProperties = casProperties;
        this.consoleAdmin = consoleAdmin;

        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        try {
            val keyManagerFactory = SSLUtil.buildKeystore(inwebo);
            this.context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), null, RandomUtils.getNativeInstance());
        } catch (final Exception e) {
            throw new RuntimeException("Cannot initialize Inwebo service", e);
        }
    }

    public LoginSearchResponse loginSearch(final String login) {
        val soap = consoleAdmin.loginSearch(login, casProperties.getAuthn().getMfa().getInwebo().getServiceId());
        val err = soap.getErr();
        val response = (LoginSearchResponse) buildResponse(new LoginSearchResponse(), "loginSearch(" + login + ")", err);
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

    public PushAuthenticateResponse pushAuthenticate(final String login) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val url = API_URL + "action=pushAuthenticate"
                + "&serviceId=" + URLEncoder.encode(StringUtils.EMPTY + inwebo.getServiceId(), StandardCharsets.UTF_8)
                + "&userId=" + URLEncoder.encode(login, StandardCharsets.UTF_8)
                + "&format=json";

        val json = call(url);
        val err = json.get("err").asText("OK");
        val response = (PushAuthenticateResponse) buildResponse(new PushAuthenticateResponse(), "pushAuthenticate(" + login + ")", err);
        if (response.isOk()) {
            val sessionId = json.get("sessionId");
            if (sessionId != null) {
                response.setSessionId(sessionId.asText());
            }
        }
        return response;
    }

    public DeviceNameResponse checkPushResult(final String login, final String sessionId) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val url = API_URL + "action=checkPushResult"
                + "&serviceId=" + URLEncoder.encode(StringUtils.EMPTY + inwebo.getServiceId(), StandardCharsets.UTF_8)
                + "&userId=" + URLEncoder.encode(login, StandardCharsets.UTF_8)
                + "&sessionId=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8)
                + "&format=json";

        val json = call(url);
        val err = json.get("err").asText("OK");
        val response = (DeviceNameResponse) buildResponse(new DeviceNameResponse(), "checkPushResult(" + login + ")", err);
        retrieveDeviceName(json, response);
        return response;
    }

    protected void retrieveDeviceName(final JsonNode json, final DeviceNameResponse response) {
        if (response.isOk()) {
            val name = json.get("name");
            if (name != null) {
                response.setDeviceName(name.asText());
            }
        }
    }

    public DeviceNameResponse authenticateExtended(final String login, final String token) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val url = API_URL + "action=authenticateExtended"
                + "&serviceId=" + URLEncoder.encode(StringUtils.EMPTY + inwebo.getServiceId(), StandardCharsets.UTF_8)
                + "&userId=" + URLEncoder.encode(login, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&format=json";

        val json = call(url);
        val err = json.get("err").asText("OK");
        val response = (DeviceNameResponse) buildResponse(new DeviceNameResponse(), "authenticateExtended(" + login + ")", err);
        retrieveDeviceName(json, response);
        return response;
    }

    protected JsonNode call(final String url) {
        try {
            val conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setSSLSocketFactory(this.context.getSocketFactory());

            conn.setRequestMethod("GET");
            return mapper.readTree(conn.getInputStream());
        } catch (final IOException e) {
            throw new RuntimeException("Inwebo call failed: " + url, e);
        }
    }

    protected AbstractResponse buildResponse(final AbstractResponse response, final String operation, final String err) {
        if ("OK".equals(err)) {
            response.setResult(Result.OK);
        } else {
            LOGGER.debug("Inwebo call: {} returned error: {}", operation, err);
            if ("NOK:NOPUSH".equals(err)) {
                response.setResult(Result.NOPUSH);
            } else if ("NOK:NOMA".equals(err)) {
                response.setResult(Result.NOMA);
            } else if ("NOK:NOLOGIN".equals(err)) {
                response.setResult(Result.NOLOGIN);
            } else if ("NOK:SN".equals(err)) {
                response.setResult(Result.SN);
            } else if ("NOK:srv unknown".equals(err)) {
                response.setResult(Result.UNKNOWN_SERVICE);
            } else if ("NOK:WAITING".equals(err)) {
                response.setResult(Result.WAITING);
            } else if ("NOK:REFUSED".equals(err)) {
                response.setResult(Result.REFUSED);
            } else if ("NOK:TIMEOUT".equals(err)) {
                response.setResult(Result.TIMEOUT);
            } else {
                response.setResult(Result.NOK);
            }
        }
        return response;
    }
}
