package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.duosecurity.client.Http;
import com.duosecurity.duoweb.DuoWebException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseDuoSecurityAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@EqualsAndHashCode(of = "properties")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseDuoSecurityAuthenticationService implements DuoSecurityAuthenticationService {
    private static final long serialVersionUID = -8044100706027708789L;

    private static final int AUTH_API_VERSION = 2;

    private static final int RESULT_CODE_ERROR_THRESHOLD = 49999;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Duo Properties.
     */
    @Getter
    protected final DuoSecurityMultifactorAuthenticationProperties properties;

    /**
     * Http client used to make calls to duo.
     */
    protected final HttpClient httpClient;

    private final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver;

    private final Cache<String, DuoSecurityUserAccount> userAccountCache;

    @Override
    public DuoSecurityAuthenticationResult authenticate(final Credential credential) throws Exception {
        if (credential instanceof DuoSecurityPasscodeCredential) {
            return authenticateDuoPasscodeCredential(credential);
        }
        if (credential instanceof DuoSecurityDirectCredential) {
            return authenticateDuoCredentialDirect(credential);
        }
        return authenticateInternal(credential);
    }

    @Override
    public DuoSecurityUserAccount getUserAccount(final String username) {
        if (!properties.isAccountStatusEnabled()) {
            LOGGER.debug("Checking Duo Security for user's [{}] account status is disabled", username);
            val account = new DuoSecurityUserAccount(username);
            account.setStatus(DuoSecurityUserAccountStatus.AUTH);
            return account;
        }

        val userAccountCachedMap = userAccountCache.asMap();
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
            LOGGER.debug("Received Duo response [{}]", jsonResponse);

            val result = MAPPER.readTree(jsonResponse);
            if (!result.has(RESULT_KEY_STAT)) {
                LOGGER.warn("Duo response was received in unknown format: [{}]", jsonResponse);
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

    @Override
    public Optional<DuoSecurityAdminApiService> getAdminApiService() {
        if (StringUtils.isNotBlank(getProperties().getDuoAdminIntegrationKey()) && StringUtils.isNotBlank(getProperties().getDuoAdminSecretKey())) {
            return Optional.of(new DefaultDuoSecurityAdminApiService(this.httpClient, properties));
        }
        return Optional.empty();
    }

    /**
     * Authenticate internal.
     *
     * @param credential the credential
     * @return the duo security authentication result
     * @throws Exception the exception
     */
    protected abstract DuoSecurityAuthenticationResult authenticateInternal(Credential credential) throws Exception;

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
            SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getDuoApiHost()),
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
            SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getDuoApiHost()),
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
     * Authenticate duo credential direct duo security authentication result.
     *
     * @param crds the credentials
     * @return the duo security authentication result
     */
    protected DuoSecurityAuthenticationResult authenticateDuoCredentialDirect(final Credential crds) {
        try {
            val credential = DuoSecurityDirectCredential.class.cast(crds);
            val principal = resolvePrincipal(credential.getPrincipal());
            val request = buildHttpPostAuthRequest();
            signHttpAuthRequest(request, principal.getId());
            val result = executeDuoApiRequest(request);
            LOGGER.debug("Duo authentication response: [{}]", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return DuoSecurityAuthenticationResult.builder().success(true).username(crds.getId()).build();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return DuoSecurityAuthenticationResult.builder().success(false).username(crds.getId()).build();
    }

    /**
     * Execute duo api request.
     *
     * @param request the request
     * @return the json object
     * @throws Exception the exception
     */
    protected JSONObject executeDuoApiRequest(final Http request) throws Exception {
        return (JSONObject) request.executeRequest();
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
        return signHttpAuthRequest(request, Map.of("username", id, "factor", "auto", "device", "auto"));
    }

    private Http signHttpAuthRequest(final Http request, final Map<String, String> parameters) {
        parameters.forEach(request::addParam);
        return signHttpUserPreAuthRequest(request);
    }

    /**
     * Sign http users request http.
     *
     * @param request the request
     * @return the http
     */
    @SneakyThrows
    protected Http signHttpUserPreAuthRequest(final Http request) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        request.signRequest(
            resolver.resolve(properties.getDuoIntegrationKey()),
            resolver.resolve(properties.getDuoSecretKey()));
        return request;
    }

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    protected Principal resolvePrincipal(final Principal principal) {
        return multifactorAuthenticationPrincipalResolver
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }

    private DuoSecurityAuthenticationResult authenticateDuoPasscodeCredential(final Credential crds) {
        try {
            val credential = DuoSecurityPasscodeCredential.class.cast(crds);
            val request = buildHttpPostAuthRequest();
            signHttpAuthRequest(request, Map.of("username", credential.getId().trim(),
                "factor", "passcode", "passcode", credential.getPassword().trim()));
            val result = executeDuoApiRequest(request);
            LOGGER.debug("Duo authentication response: [{}]", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return DuoSecurityAuthenticationResult.builder().success(true).username(crds.getId()).build();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return DuoSecurityAuthenticationResult.builder().success(false).username(crds.getId()).build();
    }
}
