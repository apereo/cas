package org.apereo.cas.adaptors.duo.authn;

import com.duosecurity.client.Http;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link BaseDuoAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseDuoAuthenticationService implements DuoAuthenticationService {

    private static final int AUTH_API_VERSION = 2;
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final String RESULT_KEY_RESPONSE = "response";
    private static final String RESULT_KEY_STAT = "stat";
    private static final long serialVersionUID = -8044100706027708789L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDuoAuthenticationService.class);
    
    /**
     * Duo Properties.
     */
    protected final MultifactorAuthenticationProperties.Duo duoProperties;

    private final transient HttpClient httpClient;

    /**
     * Creates the duo authentication service.
     *
     * @param duoProperties the duo properties
     * @param httpClient    the http client
     */
    public BaseDuoAuthenticationService(final MultifactorAuthenticationProperties.Duo duoProperties, final HttpClient httpClient) {
        this.duoProperties = duoProperties;
        this.httpClient = httpClient;
    }

    @Override
    public boolean ping() {
        try {
            final String url = buildUrlHttpScheme(getApiHost().concat("/rest/v1/ping"));
            LOGGER.debug("Contacting Duo @ [{}]", url);

            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                final String response = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                LOGGER.debug("Received Duo ping response [{}]", response);

                final JsonNode result = MAPPER.readTree(response);
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
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final BaseDuoAuthenticationService rhs = (BaseDuoAuthenticationService) obj;
        return new EqualsBuilder()
                .append(this.duoProperties.getDuoApiHost(), rhs.duoProperties.getDuoApiHost())
                .append(this.duoProperties.getDuoApplicationKey(), rhs.duoProperties.getDuoApplicationKey())
                .append(this.duoProperties.getDuoIntegrationKey(), rhs.duoProperties.getDuoIntegrationKey())
                .append(this.duoProperties.getDuoSecretKey(), rhs.duoProperties.getDuoSecretKey())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.duoProperties.getDuoApiHost())
                .append(this.duoProperties.getDuoApplicationKey())
                .append(this.duoProperties.getDuoIntegrationKey())
                .append(this.duoProperties.getDuoSecretKey())
                .toHashCode();
    }

    @Override
    public DuoUserAccountAuthStatus getDuoUserAccountAuthStatus(final String username) {
        try {
            final Http userRequest = buildHttpPostUserPreAuthRequest(username);
            signHttpUserPreAuthRequest(userRequest);
            LOGGER.debug("Contacting Duo to inquire about username [{}]", username);
            final String userResponse = userRequest.executeHttpRequest().body().string();
            final String jsonResponse = URLDecoder.decode(userResponse, StandardCharsets.UTF_8.name());
            LOGGER.debug("Received Duo admin response [{}]", jsonResponse);

            final JsonNode result = MAPPER.readTree(jsonResponse);
            if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                    && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {

                final JsonNode response = result.get(RESULT_KEY_RESPONSE);
                final String authResult = response.get("result").asText().toUpperCase();
                return DuoUserAccountAuthStatus.valueOf(authResult);
            }
        } catch (final Exception e) {
            LOGGER.warn("Reaching Duo has failed with error: [{}]", e.getMessage(), e);
        }
        return DuoUserAccountAuthStatus.AUTH;
    }

    private static String buildUrlHttpScheme(final String url) {
        if (!url.startsWith("http")) {
            return "https://" + url;
        }
        return url;
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
        final Http usersRequest = new Http(HttpMethod.POST.name(),
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
    protected Http signHttpAuthRequest(final Http request, final String id) {
        try {
            request.addParam("username", id);
            request.addParam("factor", "auto");
            request.addParam("device", "auto");
            request.signRequest(
                    duoProperties.getDuoIntegrationKey(),
                    duoProperties.getDuoSecretKey());
            return request;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Sign http users request http.
     *
     * @param request the request
     * @return the http
     */
    protected Http signHttpUserPreAuthRequest(final Http request) {
        try {
            request.signRequest(
                    duoProperties.getDuoIntegrationKey(),
                    duoProperties.getDuoSecretKey());
            return request;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
