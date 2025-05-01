package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.duosecurity.client.Http;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    private static final int AUTH_API_VERSION = 2;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final int DEFAULT_HTTP_TIMEOUT_SECONDS = 60;

    @Getter
    protected final DuoSecurityMultifactorAuthenticationProperties properties;

    protected final HttpClient httpClient;

    protected final TenantExtractor tenantExtractor;

    private final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver;

    private final Cache<String, DuoSecurityUserAccount> userAccountCache;
    
    @Override
    public DuoSecurityAuthenticationResult authenticate(final Credential credential) throws Exception {
        if (credential instanceof final DuoSecurityPasscodeCredential duo) {
            return authenticateDuoPasscodeCredential(duo);
        }
        if (credential instanceof final DuoSecurityDirectCredential duo) {
            return authenticateDuoCredentialDirect(duo);
        }
        return authenticateInternal(credential);
    }

    @Override
    public DuoSecurityUserAccount getUserAccount(final String username) {
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
            LOGGER.debug("Contacting Duo Security to inquire about username [{}]", username);
            val userResponse = getHttpResponse(userRequest);
            val jsonResponse = URLDecoder.decode(userResponse, StandardCharsets.UTF_8);
            LOGGER.debug("Received Duo response [{}]", jsonResponse);

            val result = MAPPER.readTree(jsonResponse);
            if (!result.has(RESULT_KEY_STAT)) {
                LOGGER.warn("Duo response was received in unknown format: [{}]", jsonResponse);
                throw new DuoSecurityException("Invalid response format received from Duo");
            }

            if ("OK".equalsIgnoreCase(result.get(RESULT_KEY_STAT).asText())) {
                val response = result.get(RESULT_KEY_RESPONSE);
                val authResult = response.get(RESULT_KEY_RESULT).asText().toUpperCase(Locale.ENGLISH);

                val status = DuoSecurityUserAccountStatus.valueOf(authResult);
                account.setProviderId(properties.getId());
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
                    throw new DuoSecurityException("Duo returned code %s: %s".formatted(code, result.get(RESULT_KEY_MESSAGE)));
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
        if (StringUtils.isNotBlank(properties.getDuoAdminIntegrationKey()) && StringUtils.isNotBlank(properties.getDuoAdminSecretKey())) {
            return Optional.of(new DefaultDuoSecurityAdminApiService(this.httpClient, properties));
        }
        return Optional.empty();
    }

    protected abstract DuoSecurityAuthenticationResult authenticateInternal(Credential credential) throws Exception;

    protected String getHttpResponse(final Http userRequest) throws Exception {
        try (val request = userRequest.executeHttpRequest()) {
            return Objects.requireNonNull(request.body()).string();
        }
    }

    protected Http buildHttpPostAuthRequest() throws Exception {
        val request = buildHttpRequest("/auth/v%s/auth");
        configureHttpRequest(request);
        return request;
    }

    protected Http buildHttpPostUserPreAuthRequest(final String username) throws Exception {
        val request = buildHttpRequest("/auth/v%s/preauth");
        request.addParam("username", username);
        configureHttpRequest(request);
        return request;
    }

    private Http buildHttpRequest(final String format) throws Exception {
        val originalHost = SpringExpressionLanguageValueResolver.getInstance().resolve(getDuoClient().getDuoApiHost());
        val host = new URI(StringUtils.prependIfMissing(originalHost, "https://"));
        val request = new CasHttpBuilder(HttpMethod.POST.name(),
            host.getHost(), String.format(format, AUTH_API_VERSION)).build();

        val hostField = ReflectionUtils.findField(request.getClass(), "host");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(hostField));

        val resultingHost = host.getHost() + (host.getPort() > 0 ? ":" + host.getPort() : StringUtils.EMPTY);
        ReflectionUtils.setField(hostField, request, resultingHost);

        val factory = httpClient.httpClientFactory();
        val clientInstanceBuilder = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .hostnameVerifier(factory.getHostnameVerifier());
        if ("localhost".equalsIgnoreCase(host.getHost())) {
            clientInstanceBuilder
                .certificatePinner(CertificatePinner.DEFAULT)
                .sslSocketFactory(factory.getSslContext().getSocketFactory(), (X509TrustManager) factory.getTrustManagers()[0]);
        } else {
            val pinner = getDuoClient().createCertificatePinner(host.getHost(), request);
            clientInstanceBuilder.certificatePinner(pinner);
        }
        val httpClientField = ReflectionUtils.findField(Http.class, "httpClient");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(httpClientField));

        val clientInstance = clientInstanceBuilder.build();
        ReflectionUtils.setField(httpClientField, request, clientInstance);
        return request;
    }
    
    protected void configureHttpRequest(final Http request) {
        val factory = httpClient.httpClientFactory();
        if (factory.getProxy() != null) {
            request.setProxy(factory.getProxy().getHostName(), factory.getProxy().getPort());
        }
    }

    protected DuoSecurityAuthenticationResult authenticateDuoCredentialDirect(final DuoSecurityDirectCredential credential) {
        try {
            val principal = resolvePrincipal(credential.getPrincipal());
            val request = buildHttpPostAuthRequest();
            signHttpAuthRequest(request, principal.getId());
            val result = executeDuoApiRequest(request);
            LOGGER.debug("Duo Security authentication response: [{}]", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return DuoSecurityAuthenticationResult.builder().success(true).username(credential.getId()).build();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return DuoSecurityAuthenticationResult.builder().success(false).username(credential.getId()).build();
    }

    protected JSONObject executeDuoApiRequest(final Http request) throws Exception {
        return (JSONObject) request.executeRequest();
    }

    protected Http signHttpAuthRequest(final Http request, final String id) {
        return signHttpAuthRequest(request, Map.of("username", id, "factor", "auto", "device", "auto"));
    }

    private Http signHttpAuthRequest(final Http request, final Map<String, String> parameters) {
        parameters.forEach(request::addParam);
        return signHttpUserPreAuthRequest(request);
    }

    protected Http signHttpUserPreAuthRequest(final Http request) {
        return FunctionUtils.doUnchecked(() -> {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            request.signRequest(
                resolver.resolve(getDuoClient().getDuoIntegrationKey()),
                resolver.resolve(getDuoClient().getDuoSecretKey()));
            return request;
        });
    }

    protected Principal resolvePrincipal(final Principal principal) {
        return multifactorAuthenticationPrincipalResolver
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }

    private DuoSecurityAuthenticationResult authenticateDuoPasscodeCredential(final DuoSecurityPasscodeCredential credential) {
        try {
            val request = buildHttpPostAuthRequest();
            signHttpAuthRequest(request, Map.of("username", credential.getId().trim(),
                "factor", "passcode", "passcode", credential.getPassword().trim()));
            val result = executeDuoApiRequest(request);
            LOGGER.debug("Duo authentication response: [{}]", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return DuoSecurityAuthenticationResult.builder().success(true).username(credential.getId()).build();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return DuoSecurityAuthenticationResult.builder().success(false).username(credential.getId()).build();
    }

    private static final class CasHttpBuilder extends Http.HttpBuilder {
        CasHttpBuilder(final String method, final String host, final String uri) {
            super(method, host, uri);
        }
    }
}
