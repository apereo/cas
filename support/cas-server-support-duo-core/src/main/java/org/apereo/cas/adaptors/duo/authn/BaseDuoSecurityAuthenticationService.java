package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.client.Http;
import com.duosecurity.duoweb.DuoWebException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * This is {@link BaseDuoSecurityAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@EqualsAndHashCode(of = "duoProperties")
public abstract class BaseDuoSecurityAuthenticationService implements DuoSecurityAuthenticationService {
    private static final long serialVersionUID = -8044100706027708789L;

    private static final int AUTH_API_VERSION = 2;

    private static final int RESULT_CODE_ERROR_THRESHOLD = 49999;

    private static final int USER_ACCOUNT_CACHE_INITIAL_SIZE = 50;

    private static final long USER_ACCOUNT_CACHE_MAX_SIZE = 100_000_000;

    private static final int USER_ACCOUNT_CACHE_EXPIRATION_SECONDS = 5;

    private static final String RESULT_KEY_RESPONSE = "response";

    private static final String RESULT_KEY_STAT = "stat";

    private static final String RESULT_KEY_RESULT = "result";

    private static final String RESULT_KEY_ENROLL_PORTAL_URL = "enroll_portal_url";

    private static final String RESULT_KEY_STATUS_MESSAGE = "status_msg";

    private static final String RESULT_KEY_CODE = "code";

    private static final String RESULT_KEY_MESSAGE = "message";

    private static final String RESULT_KEY_MESSAGE_DETAIL = "message_detail";

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * Duo Properties.
     */
    protected final DuoSecurityMultifactorProperties duoProperties;

    private final transient HttpClient httpClient;

    private final transient Map<String, DuoSecurityUserAccount> userAccountCachedMap;

    private final transient Cache<String, DuoSecurityUserAccount> userAccountCache;

    public BaseDuoSecurityAuthenticationService(final DuoSecurityMultifactorProperties duoProperties,
                                                final HttpClient httpClient) {
        this.duoProperties = duoProperties;
        this.httpClient = httpClient;

        this.userAccountCache = Caffeine.newBuilder()
            .initialCapacity(USER_ACCOUNT_CACHE_INITIAL_SIZE)
            .maximumSize(USER_ACCOUNT_CACHE_MAX_SIZE)
            .expireAfterWrite(Duration.ofSeconds(USER_ACCOUNT_CACHE_EXPIRATION_SECONDS))
            .build();
        this.userAccountCachedMap = this.userAccountCache.asMap();
    }

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
    public DuoSecurityUserAccount getUserAccount(final String username) {
        if (userAccountCachedMap.containsKey(username)) {
            val account = userAccountCachedMap.get(username);
            LOGGER.debug("Found cached duo user account [{}]", account);
            return account;
        }

        val account = new DuoSecurityUserAccount(username);
        account.setStatus(DuoSecurityUserAccountStatus.AUTH);

        try {
            val userRequest = buildHttpPostUserPreAuthRequest(username);
            signHttpUserPreAuthRequest(userRequest);
            LOGGER.debug("Contacting Duo to inquire about username [{}]", username);
            val userResponse = getHttpResponse(userRequest);
            val jsonResponse = URLDecoder.decode(userResponse, StandardCharsets.UTF_8.name());
            LOGGER.debug("Received Duo admin response [{}]", jsonResponse);

            val result = MAPPER.readTree(jsonResponse);
            if (!result.has(RESULT_KEY_STAT)) {
                LOGGER.warn("Duo admin response was received in unknown format: [{}]", jsonResponse);
                throw new DuoWebException("Invalid response format received from Duo");
            }

            if (result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {
                val response = result.get(RESULT_KEY_RESPONSE);
                val authResult = response.get(RESULT_KEY_RESULT).asText().toUpperCase();

                val status = DuoSecurityUserAccountStatus.valueOf(authResult);
                account.setStatus(status);
                account.setMessage(response.get(RESULT_KEY_STATUS_MESSAGE).asText());
                if (status == DuoSecurityUserAccountStatus.ENROLL) {
                    val enrollUrl = response.get(RESULT_KEY_ENROLL_PORTAL_URL).asText();
                    account.setEnrollPortalUrl(enrollUrl);
                }
            } else {
                val code = result.get(RESULT_KEY_CODE).asInt();
                if (code > RESULT_CODE_ERROR_THRESHOLD) {
                    LOGGER.warn("Duo returned a failure response with code: [{}]. Duo will be considered unavailable",
                        result.get(RESULT_KEY_MESSAGE));
                    throw new DuoWebException("Duo returned code 500: " + result.get(RESULT_KEY_MESSAGE));
                }
                LOGGER.warn("Duo returned an Invalid response with message [{}] and detail [{}] "
                        + "when determining user account. This maybe a configuration error in the admin request and Duo will "
                        + "still be considered available.",
                    result.hasNonNull(RESULT_KEY_MESSAGE) ? result.get(RESULT_KEY_MESSAGE).asText() : StringUtils.EMPTY,
                    result.hasNonNull(RESULT_KEY_MESSAGE_DETAIL) ? result.get(RESULT_KEY_MESSAGE_DETAIL).asText() : StringUtils.EMPTY);
            }
        } catch (final Exception e) {
            LOGGER.warn("Reaching Duo has failed with error: [{}]", e.getMessage(), e);
            account.setStatus(DuoSecurityUserAccountStatus.UNAVAILABLE);
        }

        userAccountCachedMap.put(account.getUsername(), account);
        LOGGER.debug("Fetched and cached duo user account [{}]", account);
        return account;
    }

    /**
     * Gets http response.
     *
     * @param userRequest the user request
     * @return the http response
     * @throws Exception the exception
     */
    protected String getHttpResponse(final Http userRequest) throws Exception {
        return userRequest.executeHttpRequest().body().string();
    }

    /**
     * Build http post auth request http.
     *
     * @return the http
     */
    protected Http buildHttpPostAuthRequest() {
        val request = new Http(HttpMethod.POST.name(),
            duoProperties.getDuoApiHost(),
            String.format("/auth/v%s/auth", AUTH_API_VERSION));
        configureHttpRequest(request);
        return request;
    }

    /**
     * Build http post get user auth request.
     *
     * @param username the username
     * @return the http
     */
    protected Http buildHttpPostUserPreAuthRequest(final String username) {
        val request = new Http(HttpMethod.POST.name(),
            duoProperties.getDuoApiHost(),
            String.format("/auth/v%s/preauth", AUTH_API_VERSION));
        request.addParam("username", username);
        configureHttpRequest(request);
        return request;
    }

    /**
     * Configure http request.
     *
     * @param request the request
     */
    protected void configureHttpRequest(final Http request) {
        val factory = this.httpClient.getHttpClientFactory();
        if (factory.getProxy() != null) {
            request.setProxy(factory.getProxy().getHostName(), factory.getProxy().getPort());
        }
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
