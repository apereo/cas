package org.apereo.cas.syncope.pm;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.syncope.SyncopeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    public boolean changeInternal(final PasswordChangeRequest bean) {
        return FunctionUtils.doAndHandle(() -> {
            val syncopeRestPasswordResetUrl = Strings.CI.appendIfMissing(
                SpringExpressionLanguageValueResolver.getInstance().resolve(
                    casProperties.getAuthn().getPm().getSyncope().getUrl()),
                "/rest/users/self/mustChangePassword");
            LOGGER.debug("Updating account password on syncope for user [{}]", bean.getUsername());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(syncopeRestPasswordResetUrl)
                .basicAuthUsername(bean.getUsername())
                .basicAuthPassword(bean.toCurrentPassword())
                .headers(Map.of(
                    SyncopeUtils.SYNCOPE_HEADER_DOMAIN, casProperties.getAuthn().getPm().getSyncope().getDomain(),
                    HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .entity(MAPPER.writeValueAsString(getPasswordPatch(bean)))
                .maximumRetryAttempts(1)
                .build();
            val response = Objects.requireNonNull(HttpUtils.execute(exec));
            if (response.getCode() == HttpStatus.SC_OK) {
                LOGGER.debug("Successfully updated the account password on Syncope for [{}]", bean.getUsername());
                return true;
            }
            return false;
        }, e -> {
            LOGGER.error("Error while update password on Syncope for user [{}]", bean.getUsername());
            return false;
        }).get();
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
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        throw new UnsupportedOperationException("Password Management Service does not support security questions");
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        throw new UnsupportedOperationException("Password Management Service does not support updating security questions");
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        throw new UnsupportedOperationException("Password Management Service does not support unlocking of accounts");
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

    private List<Map<String, List<Object>>> searchUser(final PasswordManagementQuery query) {
        return SyncopeUtils.syncopeUserSearch(casProperties.getAuthn().getPm().getSyncope(), query.getUsername());
    }

    private static JsonNode getPasswordPatch(final PasswordChangeRequest bean) {
        val passwordPatch = MAPPER.createObjectNode();
        passwordPatch.put("operation", "ADD_REPLACE");
        passwordPatch.put("value", bean.toConfirmedPassword());
        passwordPatch.put("onSyncope", true);

        val resources = MAPPER.createArrayNode();
        passwordPatch.set("resources", resources);

        return passwordPatch;
    }
}
