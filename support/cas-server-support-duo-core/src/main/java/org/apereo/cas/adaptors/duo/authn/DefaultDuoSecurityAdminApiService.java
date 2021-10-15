package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityBypassCode;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountGroup;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.DuoSecurityUserDevice;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;

import javax.net.ssl.X509TrustManager;
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

    private static DuoSecurityUserAccount mapDuoSecurityUserAccount(final JSONObject userJson) throws JSONException {
        val user = new DuoSecurityUserAccount(userJson.getString("username"));
        user.setStatus(DuoSecurityUserAccountStatus.from(userJson.getString("status")));
        FunctionUtils.doIfNotNull(userJson.get("email"), value -> user.addAttribute("email", value.toString()));
        FunctionUtils.doIfNotNull(userJson.getString("user_id"), value -> user.addAttribute("user_id", value));
        FunctionUtils.doIfNotNull(userJson.get("firstname"), value -> user.addAttribute("firstname", value.toString()));
        FunctionUtils.doIfNotNull(userJson.get("lastname"), value -> user.addAttribute("lastname", value.toString()));
        FunctionUtils.doIfNotNull(userJson.get("realname"), value -> user.addAttribute("realname", value.toString()));
        FunctionUtils.doIfNotNull(userJson.getBoolean("is_enrolled"), value -> user.addAttribute("is_enrolled", value.toString()));
        FunctionUtils.doIfNotNull(userJson.getLong("last_login"), value -> user.addAttribute("last_login", value.toString()));
        FunctionUtils.doIfNotNull(userJson.getLong("created"), value -> user.addAttribute("created", value.toString()));
        FunctionUtils.doIfNotNull(userJson.optString("alias1"), value -> user.addAttribute("alias1", value));
        FunctionUtils.doIfNotNull(userJson.optString("alias2"), value -> user.addAttribute("alias2", value));
        if (user.getStatus() != DuoSecurityUserAccountStatus.DENY && !user.isEnrolled()) {
            user.setStatus(DuoSecurityUserAccountStatus.ENROLL);
        }
        mapUserPhones(userJson, user);
        mapUserGroups(userJson, user);
        return user;
    }

    private static void mapUserGroups(final JSONObject userJson, final DuoSecurityUserAccount user) throws JSONException {
        val groupsResponse = userJson.getJSONArray("groups");
        for (int i = 0; groupsResponse != null && i < groupsResponse.length(); i++) {
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
        for (int i = 0; phones != null && i < phones.length(); i++) {
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

    @Override
    public Optional<DuoSecurityUserAccount> getDuoSecurityUserAccount(final String username) throws Exception {
        val userResponse = getEndpointResultFor(CollectionUtils.wrap("uri", "users", "username", username));
        if (userResponse != null && userResponse.length() == 1) {
            val userJson = userResponse.getJSONObject(0);
            val user = mapDuoSecurityUserAccount(userJson);
            user.addBypassCodes(getDuoSecurityBypassCodesFor(user.getUserId()));
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public List<Long> createDuoSecurityBypassCodesFor(final String userIdentifier) throws Exception {
        val params = CollectionUtils.<String, String>wrap("uri", String.format("users/%s/bypass_codes", userIdentifier));
        params.put("method", HttpMethod.POST.name());
        val bypassResponse = getEndpointResultFor(params);
        if (bypassResponse != null) {
            return Arrays.stream(bypassResponse.join(",")
                    .replace("\"", StringUtils.EMPTY).split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    @Override
    public List<DuoSecurityBypassCode> getDuoSecurityBypassCodesFor(final String userIdentifier) throws Exception {
        val codes = new ArrayList<DuoSecurityBypassCode>();

        val bypassResponse = getEndpointResultFor(CollectionUtils.wrap("uri", String.format("users/%s/bypass_codes", userIdentifier)));
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

    private JSONArray getEndpointResultFor(final Map<String, String> params) throws Exception {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val uri = getAdminEndpointUri(params.getOrDefault("uri", StringUtils.EMPTY));
        val method = params.getOrDefault("method", HttpMethod.GET.name());
        val request = new Http(method, resolver.resolve(duoProperties.getDuoApiHost()), uri);
        
        val factory = this.httpClient.getHttpClientFactory();
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
        val httpClientField = ReflectionUtils.findField(request.getClass(), "httpClient");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(httpClientField));
        ReflectionUtils.setField(httpClientField, request, okHttpClient);

        val result = (JSONObject) request.executeJSONRequest();
        return result.length() > 0 && result.has(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE)
            && result.has(DuoSecurityAuthenticationService.RESULT_KEY_STAT)
            ? result.getJSONArray(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE)
            : null;
    }
}
