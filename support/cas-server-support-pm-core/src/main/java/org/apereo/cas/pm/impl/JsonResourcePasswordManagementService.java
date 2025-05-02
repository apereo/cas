package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link JsonResourcePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonResourcePasswordManagementService extends BasePasswordManagementService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final Resource jsonResource;

    private Map<String, JsonBackedAccount> jsonBackedAccounts;

    public JsonResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                 final CasConfigurationProperties casProperties,
                                                 final Resource jsonResource,
                                                 final PasswordHistoryService passwordHistoryService) {
        super(casProperties, cipherExecutor, passwordHistoryService);
        this.jsonResource = jsonResource;
        readAccountsFromJsonResource();
    }

    @Override
    public boolean changeInternal(final @NonNull PasswordChangeRequest bean) {
        if (StringUtils.isBlank(bean.toPassword())) {
            LOGGER.error("Password cannot be blank");
            return false;
        }
        if (!StringUtils.equals(bean.toPassword(), bean.toConfirmedPassword())) {
            LOGGER.error("Password does not match and cannot be confirmed");
            return false;
        }
        val account = jsonBackedAccounts.getOrDefault(bean.getUsername(), null);
        if (account == null) {
            LOGGER.error("User account [{}] cannot be found", bean.getUsername());
            return false;
        }
        account.setPassword(bean.toPassword());
        jsonBackedAccounts.put(bean.getUsername(), account);
        return writeAccountToJsonResource();
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        val account = jsonBackedAccounts.getOrDefault(query.getUsername(), null);
        return Optional.ofNullable(account).map(JsonBackedAccount::getEmail).orElse(null);
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        val account = jsonBackedAccounts.getOrDefault(query.getUsername(), null);
        return Optional.ofNullable(account).map(JsonBackedAccount::getPhone).orElse(null);
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        val result = jsonBackedAccounts.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getEmail().equalsIgnoreCase(query.getEmail()))
            .findFirst();
        return result.map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val account = jsonBackedAccounts.getOrDefault(query.getUsername(), null);
        if (account != null) {
            return account.getSecurityQuestions();
        }
        return new HashMap<>();
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        val account = jsonBackedAccounts.getOrDefault(query.getUsername(), null);
        if (account != null) {
            account.setSecurityQuestions(query.getSecurityQuestions().toSingleValueMap());
            writeAccountToJsonResource();
        }
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        val account = jsonBackedAccounts.getOrDefault(credential.getId(), null);
        if (account != null && "locked".equalsIgnoreCase(account.getStatus())) {
            account.setStatus("OK");
            writeAccountToJsonResource();
        }
        return true;
    }

    @Data
    @SuppressWarnings("UnusedMethod")
    public static class JsonBackedAccount implements Serializable {
        @Serial
        private static final long serialVersionUID = -8522936598053838986L;

        private String email;

        private String password;

        private String phone;

        private String status;

        private Map<String, String> securityQuestions = new HashMap<>();
    }

    private boolean writeAccountToJsonResource() {
        return FunctionUtils.doUnchecked(() -> {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), jsonBackedAccounts);
            readAccountsFromJsonResource();
            return true;
        });
    }

    private void readAccountsFromJsonResource() {
        FunctionUtils.doUnchecked(__ -> {
            try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                val personList = new TypeReference<Map<String, JsonBackedAccount>>() {
                };
                jsonBackedAccounts = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            }
        });
    }
}
