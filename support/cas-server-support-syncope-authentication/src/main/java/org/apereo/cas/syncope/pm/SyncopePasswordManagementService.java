package org.apereo.cas.syncope.pm;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.syncope.SyncopeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link SyncopePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class SyncopePasswordManagementService extends BasePasswordManagementService {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public SyncopePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                            final CasConfigurationProperties casProperties,
                                            final PasswordHistoryService passwordHistoryService) {
        super(casProperties, cipherExecutor, passwordHistoryService);
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) throws Throwable {
        val url = determinePasswordResetUrl(bean);
        LOGGER.debug("Updating account password on Apache Syncope for user [{}]", bean.getUsername());
        val httpRequest = buildPasswordChangeHttpRequest(bean, url);
        val response = Objects.requireNonNull(HttpUtils.execute(httpRequest));
        if (response.getCode() == HttpStatus.SC_OK) {
            LOGGER.debug("Successfully updated the account password on Apache Syncope for [{}]", bean.getUsername());
            return true;
        }
        return false;
    }

    protected HttpExecutionRequest buildPasswordChangeHttpRequest(final PasswordChangeRequest bean, final String url) throws Throwable {
        LOGGER.debug("Changing password for user [{}] via [{}]", bean.getUsername(), url);
        if (StringUtils.isBlank(bean.toCurrentPassword())) {
            val userKey = UriComponentsBuilder.fromUriString(url).build().getPathSegments().getLast();
            return HttpExecutionRequest.builder()
                .method(HttpMethod.PATCH)
                .url(url)
                .basicAuthUsername(casProperties.getAuthn().getPm().getSyncope().getBasicAuthUsername())
                .basicAuthPassword(casProperties.getAuthn().getPm().getSyncope().getBasicAuthPassword())
                .headers(Map.of(
                    SyncopeUtils.SYNCOPE_HEADER_DOMAIN, casProperties.getAuthn().getPm().getSyncope().getDomain(),
                    HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .entity(MAPPER.writeValueAsString(getUserPasswordUpdateRequest(bean, userKey)))
                .maximumRetryAttempts(1)
                .build();
        }

        return HttpExecutionRequest.builder()
            .method(HttpMethod.POST)
            .url(url)
            .basicAuthUsername(bean.getUsername())
            .basicAuthPassword(bean.toCurrentPassword())
            .headers(Map.of(
                SyncopeUtils.SYNCOPE_HEADER_DOMAIN, casProperties.getAuthn().getPm().getSyncope().getDomain(),
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .entity(MAPPER.writeValueAsString(getPasswordPatch(bean)))
            .maximumRetryAttempts(1)
            .build();
    }

    private String determinePasswordResetUrl(final PasswordChangeRequest bean) {
        val currentPassword = bean.toCurrentPassword();
        if (StringUtils.isBlank(currentPassword)) {
            val userKey = fetchSyncopeUserKey(bean.getUsername());
            return Strings.CI.appendIfMissing(
                SpringExpressionLanguageValueResolver.getInstance().resolve(
                    casProperties.getAuthn().getPm().getSyncope().getUrl()),
                "/rest/users/%s".formatted(userKey));
        }

        return Strings.CI.appendIfMissing(
            SpringExpressionLanguageValueResolver.getInstance().resolve(
                casProperties.getAuthn().getPm().getSyncope().getUrl()),
            "/rest/users/self/mustChangePassword");
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        return getUserAttribute(query, "email").orElseGet(query::getEmail);
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        return getUserAttribute(query, "phoneNumber").orElseGet(query::getEmail);
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        return getUserAttribute(query, "username").orElseGet(query::getEmail);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
        val questionKey = searchUser(PasswordManagementQuery.builder().username(query.getUsername()).build())
            .stream()
            .findFirst()
            .map(syncopeUser -> syncopeUser.getOrDefault("securityQuestion", syncopeUser.get("syncopeUserSecurityQuestion")))
            .filter(Objects::nonNull)
            .filter(values -> !values.isEmpty())
            .map(values -> values.getFirst().toString())
            .orElseThrow();

        val securityQuestionUrl = Strings.CI.appendIfMissing(
            SpringExpressionLanguageValueResolver.getInstance().resolve(
                casProperties.getAuthn().getPm().getSyncope().getUrl()),
            "/rest/securityQuestions/%s".formatted(questionKey));
        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.GET)
            .url(securityQuestionUrl)
            .basicAuthUsername(casProperties.getAuthn().getPm().getSyncope().getBasicAuthUsername())
            .basicAuthPassword(casProperties.getAuthn().getPm().getSyncope().getBasicAuthPassword())
            .headers(Map.of(
                SyncopeUtils.SYNCOPE_HEADER_DOMAIN, casProperties.getAuthn().getPm().getSyncope().getDomain(),
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .build();
        val response = Objects.requireNonNull(HttpUtils.execute(exec));
        if (org.springframework.http.HttpStatus.resolve(response.getCode()).is2xxSuccessful()
            && response instanceof final HttpEntityContainer container) {
            val entity = container.getEntity();
            val result = EntityUtils.toString(entity);
            LOGGER.debug("Received security question entity as [{}]", result);
            return Map.of(MAPPER.readTree(result).get("content").asText(), UUID.randomUUID().toString());
        }
        return Map.of();
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        throw new UnsupportedOperationException("Password Management Service does not support updating security questions");
    }

    @Override
    public boolean unlockAccount(final Credential credential) throws Throwable {
        val userKey = fetchSyncopeUserKey(credential.getId());
        val userStatusUrl = Strings.CI.appendIfMissing(
            SpringExpressionLanguageValueResolver.getInstance().resolve(
                casProperties.getAuthn().getPm().getSyncope().getUrl()),
            "/rest/users/%s/status".formatted(userKey));

        LOGGER.debug("Updating account status on Apache Syncope for user [{}]", credential.getId());
        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.POST)
            .url(userStatusUrl)
            .basicAuthUsername(casProperties.getAuthn().getPm().getSyncope().getBasicAuthUsername())
            .basicAuthPassword(casProperties.getAuthn().getPm().getSyncope().getBasicAuthPassword())
            .headers(Map.of(
                SyncopeUtils.SYNCOPE_HEADER_DOMAIN, casProperties.getAuthn().getPm().getSyncope().getDomain(),
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .entity(MAPPER.writeValueAsString(getUserStatusUpdatePatch(userKey)))
            .maximumRetryAttempts(1)
            .build();
        val response = Objects.requireNonNull(HttpUtils.execute(exec));
        if (org.springframework.http.HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
            LOGGER.debug("Successfully updated the account status on Apache Syncope for [{}]", credential.getId());
            return true;
        }
        return false;
    }

    protected String fetchSyncopeUserKey(final String username) {
        val query = PasswordManagementQuery.builder().username(username).build();
        return searchUser(query)
            .stream()
            .findFirst()
            .map(syncopeUser -> syncopeUser.getOrDefault("key", syncopeUser.get("syncopeUserKey")))
            .filter(Objects::nonNull)
            .filter(values -> !values.isEmpty())
            .map(values -> values.getFirst().toString())
            .orElseThrow(() -> new IllegalArgumentException("No user or its key can be found for username: " + username));
    }

    protected Optional<String> getUserAttribute(final PasswordManagementQuery query, final String attributeName) {
        return searchUser(query)
            .stream()
            .findFirst()
            .map(syncopeUser -> {
                val prefix = "%s_%s".formatted("syncopeUserAttr", attributeName);
                return syncopeUser.getOrDefault(attributeName, syncopeUser.get(prefix));
            })
            .filter(Objects::nonNull)
            .filter(values -> !values.isEmpty())
            .map(values -> values.getFirst().toString());
    }

    protected List<Map<String, List<Object>>> searchUser(final PasswordManagementQuery query) {
        return SyncopeUtils.syncopeUserSearch(casProperties.getAuthn().getPm().getSyncope(), query.getUsername());
    }

    protected static JsonNode getPasswordPatch(final PasswordChangeRequest bean) {
        val passwordPatch = MAPPER.createObjectNode();
        passwordPatch.put("operation", "ADD_REPLACE");
        passwordPatch.put("value", bean.toConfirmedPassword());
        passwordPatch.put("onSyncope", true);
        passwordPatch.set("resources", MAPPER.createArrayNode());
        return passwordPatch;
    }

    protected static JsonNode getUserPasswordUpdateRequest(final PasswordChangeRequest bean,
                                                           final String userKey) {
        val userPatch = MAPPER.createObjectNode();
        userPatch.put("_class", "org.apache.syncope.common.lib.request.UserUR");
        userPatch.put("key", userKey);
        userPatch.set("password", getPasswordPatch(bean));
        return userPatch;
    }

    protected static JsonNode getUserStatusUpdatePatch(final String userKey) {
        val passwordPatch = MAPPER.createObjectNode();
        passwordPatch.put("operation", "ADD_REPLACE");
        passwordPatch.put("key", userKey);
        passwordPatch.put("type", "REACTIVATE");
        passwordPatch.put("onSyncope", true);
        passwordPatch.set("resources", MAPPER.createArrayNode());
        return passwordPatch;
    }
}
