package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityBypassCode;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountGroup;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.DuoSecurityUserDevice;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.duosecurity.client.Http;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultDuoSecurityAdminApiService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class DefaultDuoSecurityAdminApiService implements DuoSecurityAdminApiService {
    private static final long TIMEOUT_SECONDS = 60;

    private final HttpClient httpClient;

    private final DuoSecurityMultifactorAuthenticationProperties duoProperties;

    private static String getAdminEndpointUri(final String uri) {
        return "/admin/v1/" + uri;
    }

    @Override
    public Optional<DuoSecurityUserAccount> getDuoSecurityUserAccount(final String username, final boolean fetchBypassCodes) throws Exception {
        val userResponse = (JSONArray) executeAdminEndpoint(CollectionUtils.wrap("uri", "users", "username", username));
        if (userResponse != null && userResponse.length() == 1) {
            val userJson = userResponse.getJSONObject(0);
            val user = mapDuoSecurityUserAccount(userJson);
            if (fetchBypassCodes) {
                user.addBypassCodes(getDuoSecurityBypassCodesFor(user.getUserId()));
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public Optional<DuoSecurityUserAccount> modifyDuoSecurityUserAccount(final DuoSecurityUserAccount newAccount) throws Exception {
        val userResponse = (JSONArray) executeAdminEndpoint(CollectionUtils.wrap("uri", "users", "username", newAccount.getUsername()));
        if (userResponse != null && userResponse.length() == 1) {
            val user = mapDuoSecurityUserAccount(userResponse.getJSONObject(0));
            val parameters = CollectionUtils.<String, String>wrap(
                "method", HttpMethod.POST.name(),
                "uri", "users/%s".formatted(user.getUserId()));
            FunctionUtils.doIfNotNull(newAccount.getEmail(), __ -> parameters.put("email", newAccount.getEmail()));
            FunctionUtils.doIfNotNull(newAccount.getFirstName(), __ -> parameters.put("firstname", newAccount.getFirstName()));
            FunctionUtils.doIfNotNull(newAccount.getLastName(), __ -> parameters.put("lastname", newAccount.getLastName()));
            FunctionUtils.doIfNotNull(newAccount.getStatus(), __ -> parameters.put("status", newAccount.getStatus().toValue()));
            val updateResponse = executeAdminEndpoint(parameters);
            val userAccount = (JSONObject) (updateResponse instanceof final JSONArray array ? array.get(0) : updateResponse);
            return Optional.of(mapDuoSecurityUserAccount(userAccount));
        }
        return Optional.empty();
    }

    @Override
    public void deleteDuoSecurityUserDevice(final String username, final String deviceId) throws Exception {
        val userIdentifier = getDuoSecurityUserAccount(username, false).map(DuoSecurityUserAccount::getUserId).orElseThrow();
        val params = CollectionUtils.<String, String>wrap("uri", String.format("users/%s/phones/%s", userIdentifier, deviceId));
        params.put("method", HttpMethod.DELETE.name());
        executeAdminEndpoint(params);
    }

    @Override
    public List<Long> createDuoSecurityBypassCodesFor(final String userIdentifier) throws Exception {
        val params = CollectionUtils.<String, String>wrap("uri", String.format("users/%s/bypass_codes", userIdentifier));
        params.put("method", HttpMethod.POST.name());
        val bypassResponse = (JSONArray) executeAdminEndpoint(params);
        if (bypassResponse != null) {
            return Arrays.stream(bypassResponse.join(",")
                    .replace("\"", StringUtils.EMPTY).split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<DuoSecurityBypassCode> getDuoSecurityBypassCodesFor(final String userIdentifier) throws Exception {
        val codes = new ArrayList<DuoSecurityBypassCode>();

        val bypassResponse = (JSONArray) executeAdminEndpoint(CollectionUtils.wrap("uri", String.format("users/%s/bypass_codes", userIdentifier)));
        for (var i = 0; bypassResponse != null && i < bypassResponse.length(); i++) {
            val bypassJson = bypassResponse.getJSONObject(i);
            if (bypassJson.has("bypass_code_id")) {
                val code = new DuoSecurityBypassCode(bypassJson.getString("bypass_code_id"));
                code.setCreated(bypassJson.optLong("created"));
                code.setExpiration(bypassJson.optLong("expiration"));
                code.setReuseCount(bypassJson.optLong("reuse_count"));
                code.setCreatedBy(bypassJson.optString("admin_email"));
                codes.add(code);
            }
        }
        return codes;
    }

    protected void prepareHttpRequest(final Http request) {
    }

    private Object executeAdminEndpoint(final Map<String, String> params) throws Exception {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val uri = getAdminEndpointUri(params.getOrDefault("uri", StringUtils.EMPTY));
        val method = params.getOrDefault("method", HttpMethod.GET.name());

        val originalHost = resolver.resolve(duoProperties.getDuoApiHost());
        val host = new URI(Strings.CI.prependIfMissing(originalHost, "https://"));
        val request = new CasHttpBuilder(method, host.getHost(), uri).build();

        val hostField = ReflectionUtils.findField(request.getClass(), "host");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(hostField));

        val resultingHost = host.getHost() + (host.getPort() > 0 ? ":" + host.getPort() : StringUtils.EMPTY);
        ReflectionUtils.setField(hostField, request, resultingHost);

        val factory = httpClient.httpClientFactory();
        val okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .sslSocketFactory(factory.getSslContext().getSocketFactory(), (X509TrustManager) factory.getTrustManagers()[0])
            .hostnameVerifier(factory.getHostnameVerifier())
            .build();

        request.addParam("offset", "0");
        request.addParam("limit", params.getOrDefault("limit", "1"));
        params.forEach(request::addParam);
        val ikey = resolver.resolve(duoProperties.getDuoAdminIntegrationKey());
        val skey = resolver.resolve(duoProperties.getDuoAdminSecretKey());
        request.signRequest(ikey, skey);
        Optional.ofNullable(factory.getProxy()).ifPresent(proxy -> request.setProxy(proxy.getHostName(), proxy.getPort()));
        val httpClientField = ReflectionUtils.findField(request.getClass(), HttpClient.BEAN_NAME_HTTPCLIENT);
        ReflectionUtils.makeAccessible(Objects.requireNonNull(httpClientField));
        ReflectionUtils.setField(httpClientField, request, okHttpClient);

        prepareHttpRequest(request);

        val result = (JSONObject) request.executeJSONRequest();
        if (!result.isEmpty() && result.has(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE)
            && result.has(DuoSecurityAuthenticationService.RESULT_KEY_STAT)) {
            val response = result.optJSONArray(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE);
            if (response == null) {
                return result.optJSONObject(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE, result);
            }
            return response;
        }
        return null;
    }

    private static final class CasHttpBuilder extends Http.HttpBuilder {
        CasHttpBuilder(final String method, final String host, final String uri) {
            super(method, host, uri);
        }
    }

    protected DuoSecurityUserAccount mapDuoSecurityUserAccount(final JSONObject userJson) throws JSONException {
        val user = new DuoSecurityUserAccount(userJson.getString("username"));
        user.setUserId(userJson.getString("user_id"));
        user.setStatus(DuoSecurityUserAccountStatus.from(userJson.getString("status")));

        collectDuoSecurityUserAccountAttribute(userJson, "email", user);
        collectDuoSecurityUserAccountAttribute(userJson, "user_id", user);
        collectDuoSecurityUserAccountAttribute(userJson, "firstname", user);
        collectDuoSecurityUserAccountAttribute(userJson, "lastname", user);
        collectDuoSecurityUserAccountAttribute(userJson, "realname", user);
        collectDuoSecurityUserAccountAttribute(userJson, "is_enrolled", user);
        collectDuoSecurityUserAccountAttribute(userJson, "last_login", user);
        collectDuoSecurityUserAccountAttribute(userJson, "created", user);
        collectDuoSecurityUserAccountAttribute(userJson, "alias1", user);
        collectDuoSecurityUserAccountAttribute(userJson, "alias2", user);
        collectDuoSecurityUserAccountAttribute(userJson, "notes", user);
        collectDuoSecurityUserAccountAttribute(userJson, "lockout_reason", user);

        if (user.getStatus() != DuoSecurityUserAccountStatus.DENY && !user.isEnrolled()) {
            user.setStatus(DuoSecurityUserAccountStatus.ENROLL);
        }
        user.setProviderId(duoProperties.getId());
        mapUserPhones(userJson, user);
        mapUserGroups(userJson, user);
        return user;
    }

    private static void collectDuoSecurityUserAccountAttribute(final JSONObject userJson,
                                                               final String attribute,
                                                               final DuoSecurityUserAccount user) {
        FunctionUtils.doIfNotNull(userJson.opt(attribute), value -> user.addAttribute(attribute, value.toString()));
    }

    private static void mapUserGroups(final JSONObject userJson, final DuoSecurityUserAccount user) throws JSONException {
        val groupsResponse = userJson.getJSONArray("groups");
        for (var i = 0; groupsResponse != null && i < groupsResponse.length(); i++) {
            val json = groupsResponse.getJSONObject(i);
            if (json.has("group_id")) {
                val group = new DuoSecurityUserAccountGroup(json.getString("group_id"),
                    json.getString("name"), json.getString("status"));
                group.setDescription(json.optString("description"));
                group.setMobileOtpEnabled(json.optBoolean("mobile_otp_enabled"));
                group.setPushEnabled(json.optBoolean("push_enabled"));
                group.setSmsEnabled(json.optBoolean("sms_enabled"));
                group.setVoiceEnabled(json.optBoolean("voice_enabled"));
                user.addGroup(group);
            }
        }
    }

    private static void mapUserPhones(final JSONObject userJson, final DuoSecurityUserAccount user) throws JSONException {
        val phones = userJson.getJSONArray("phones");
        for (var i = 0; phones != null && i < phones.length(); i++) {
            val phoneJson = phones.getJSONObject(i);
            val phone = new DuoSecurityUserDevice(phoneJson.getString("name"), phoneJson.getString("type"));
            phone.setActivated(phoneJson.optBoolean("activated"));
            phone.setLastSeen(phoneJson.optString("last_seen"));
            phone.setModel(phoneJson.optString("model"));
            phone.setNumber(phoneJson.optString("number"));
            phone.setPlatform(phoneJson.optString("platform"));
            phone.setId(phoneJson.optString("phone_id"));
            phone.setCapabilities(List.of(phoneJson.getJSONArray("capabilities")
                .join(",")
                .replace("\"", StringUtils.EMPTY)
                .split(",")));
            user.addDevice(phone);
        }
    }
}
