package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.client.Http;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link BaseDuoSecurityAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(of = "duoProperties")
public abstract class BaseDuoSecurityAuthenticationService implements DuoSecurityAuthenticationService {
    private static final long serialVersionUID = -8044100706027708789L;

    private static final int AUTH_API_VERSION = 2;
    private static final String RESULT_KEY_RESPONSE = "response";
    private static final String RESULT_KEY_STAT = "stat";
    private static final String RESULT_KEY_RESULT = "result";
    private static final String RESULT_KEY_ENROLL_PORTAL_URL = "enroll_portal_url";
    private static final String RESULT_KEY_STATUS_MESSAGE = "status_msg";

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * Duo Properties.
     */
    protected final DuoSecurityMultifactorProperties duoProperties;

    private final transient HttpClient httpClient;

    private static String buildUrlHttpScheme(final String url) {
        if (!url.startsWith("http")) {
            return "https://" + url;
        }
        return url;
    }

    @Override
    public boolean ping() {
        try {
            val url = buildUrlHttpScheme(getApiHost().concat("/rest/v1/ping"));
            LOGGER.debug("Contacting Duo @ [{}]", url);

            val msg = this.httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                val response = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                LOGGER.debug("Received Duo ping response [{}]", response);

                val result = MAPPER.readTree(response);
                if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                    && result.get(RESULT_KEY_RESPONSE).asText().equalsIgnoreCase("pong")
                    && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {
                    return true;
                }
                LOGGER.warn("Could not reach/ping Duo. Response returned is [{}]", result);
            }
        } catch (final Exception e) {
            LOGGER.warn("Pinging Duo has failed with error: [{}]", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String getApiHost() {
        return duoProperties.getDuoApiHost();
    }

    @Override
    public DuoUserAccount getDuoUserAccount(final String username) {
        val account = new DuoUserAccount(username);
        account.setStatus(DuoUserAccountAuthStatus.AUTH);

        try {
            val userRequest = buildHttpPostUserPreAuthRequest(username);
            signHttpUserPreAuthRequest(userRequest);
            LOGGER.debug("Contacting Duo to inquire about username [{}]", username);
            val userResponse = userRequest.executeHttpRequest().body().string();
            val jsonResponse = URLDecoder.decode(userResponse, StandardCharsets.UTF_8.name());
            LOGGER.debug("Received Duo admin response [{}]", jsonResponse);

            val result = MAPPER.readTree(jsonResponse);
            if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {

                val response = result.get(RESULT_KEY_RESPONSE);
                val authResult = response.get(RESULT_KEY_RESULT).asText().toUpperCase();

                val status = DuoUserAccountAuthStatus.valueOf(authResult);
                account.setStatus(status);
                account.setMessage(response.get(RESULT_KEY_STATUS_MESSAGE).asText());
                if (status == DuoUserAccountAuthStatus.ENROLL) {
                    val enrollUrl = response.get(RESULT_KEY_ENROLL_PORTAL_URL).asText();
                    account.setEnrollPortalUrl(enrollUrl);
                }
            }
        } catch (final Exception e) {
            LOGGER.warn("Reaching Duo has failed with error: [{}]", e.getMessage(), e);
        }
        return account;
    }

    /**
     * Build http post auth request http.
     *
     * @return the http
     */
    protected Http buildHttpPostAuthRequest() {
        return new Http(HttpMethod.POST.name(),
            duoProperties.getDuoApiHost(),
            String.format("/auth/v%s/auth", AUTH_API_VERSION));
    }

    /**
     * Build http post get user auth request.
     *
     * @param username the username
     * @return the http
     */
    protected Http buildHttpPostUserPreAuthRequest(final String username) {
        val usersRequest = new Http(HttpMethod.POST.name(),
            duoProperties.getDuoApiHost(),
            String.format("/auth/v%s/preauth", AUTH_API_VERSION));
        usersRequest.addParam("username", username);
        return usersRequest;
    }


    /**
     * Sign http request.
     *
     * @param request the request
     * @param id      the id
     * @return the http
     */
    @SneakyThrows
    protected Http signHttpAuthRequest(final Http request, final String id) {
        request.addParam("username", id);
        request.addParam("factor", "auto");
        request.addParam("device", "auto");
        request.signRequest(
            duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey());
        return request;
    }

    /**
     * Sign http users request http.
     *
     * @param request the request
     * @return the http
     */
    @SneakyThrows
    protected Http signHttpUserPreAuthRequest(final Http request) {
        request.signRequest(
            duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey());
        return request;
    }
}
