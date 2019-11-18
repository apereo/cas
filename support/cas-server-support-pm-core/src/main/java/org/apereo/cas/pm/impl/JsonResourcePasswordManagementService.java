package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
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
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Resource jsonResource;

    private Map<String, JsonBackedAccount> jsonBackedAccounts;

    public JsonResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                 final String issuer,
                                                 final PasswordManagementProperties passwordManagementProperties,
                                                 final Resource jsonResource,
                                                 final PasswordHistoryService passwordHistoryService) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.jsonResource = jsonResource;
        readAccountsFromJsonResource();
    }

    @Override
    public boolean changeInternal(final @NonNull Credential credential, final @NonNull PasswordChangeRequest bean) {
        val c = (UsernamePasswordCredential) credential;
        if (StringUtils.isBlank(bean.getPassword())) {
            LOGGER.error("Password cannot be blank");
            return false;
        }
        if (!StringUtils.equals(bean.getPassword(), bean.getConfirmedPassword())) {
            LOGGER.error("Password does not match and cannot be confirmed");
            return false;
        }
        val account = this.jsonBackedAccounts.getOrDefault(c.getId(), null);
        if (account == null) {
            LOGGER.error("User account [{}] cannot be found", c.getId());
            return false;
        }
        account.setPassword(bean.getPassword());
        this.jsonBackedAccounts.put(c.getId(), account);
        return writeAccountToJsonResource();
    }

    @SneakyThrows
    private boolean writeAccountToJsonResource() {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(this.jsonResource.getFile(), this.jsonBackedAccounts);
        readAccountsFromJsonResource();
        return true;
    }

    @Override
    public String findEmail(final String username) {
        val account = this.jsonBackedAccounts.getOrDefault(username, null);
        return Optional.ofNullable(account).map(JsonBackedAccount::getEmail).orElse(null);
    }

    @Override
    public String findPhone(final String username) {
        val account = this.jsonBackedAccounts.getOrDefault(username, null);
        return Optional.ofNullable(account).map(JsonBackedAccount::getPhone).orElse(null);
    }

    @Override
    public String findUsername(final String email) {
        val result = this.jsonBackedAccounts.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getEmail().equalsIgnoreCase(email))
            .findFirst();
        return result.map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        val account = this.jsonBackedAccounts.getOrDefault(username, null);
        if (account != null) {
            return account.getSecurityQuestions();
        }
        return new HashMap<>(0);
    }

    private void readAccountsFromJsonResource() {
        try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
            final TypeReference<Map<String, JsonBackedAccount>> personList = new TypeReference<>() {
            };
            this.jsonBackedAccounts = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Data
    private static class JsonBackedAccount implements Serializable {
        private static final long serialVersionUID = -8522936598053838986L;

        private String email;

        private String password;

        private String phone;
        
        private Map<String, String> securityQuestions = new HashMap<>(0);
    }
}
