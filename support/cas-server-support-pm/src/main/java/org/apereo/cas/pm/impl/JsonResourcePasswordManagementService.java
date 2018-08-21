package org.apereo.cas.pm.impl;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;

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
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
                                                 final String issuer, final PasswordManagementProperties passwordManagementProperties,
                                                 final Resource jsonResource) {
        super(passwordManagementProperties, cipherExecutor, issuer);
        this.jsonResource = jsonResource;
        readAccountsFromJsonResource();
    }

    private void readAccountsFromJsonResource() {
        try (Reader reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
            final TypeReference<Map<String, JsonBackedAccount>> personList = new TypeReference<>() {
            };
            this.jsonBackedAccounts = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean changeInternal(@NonNull final Credential credential, @NonNull final PasswordChangeBean bean) {
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
        return account == null ? null : account.getEmail();
    }

    @Override
    public String findUsername(final String email) {
        val result = this.jsonBackedAccounts.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getEmail().equalsIgnoreCase(email))
            .findFirst();
        return result.isPresent() ? result.get().getKey() : null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        val account = this.jsonBackedAccounts.getOrDefault(username, null);
        if (account != null) {
            return account.getSecurityQuestions();
        }
        return new HashMap<>(0);
    }

    /**
     * The type Json backed account.
     */
    @Data
    private static class JsonBackedAccount {
        private String email;

        private String password;

        private Map<String, String> securityQuestions = new HashMap<>();
    }
}
