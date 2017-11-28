package org.apereo.cas.pm.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.hjson.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

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
public class JsonResourcePasswordManagementService extends BasePasswordManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResourcePasswordManagementService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Resource jsonResource;
    private Map<String, JsonBackedAccount> jsonBackedAccounts;

    public JsonResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                 final String issuer,
                                                 final PasswordManagementProperties passwordManagementProperties,
                                                 final Resource jsonResource) {
        super(cipherExecutor, issuer, passwordManagementProperties);
        this.jsonResource = jsonResource;
        readAccountsFromJsonResource();
    }

    private void readAccountsFromJsonResource() {
        try (Reader reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
            final TypeReference<Map<String, JsonBackedAccount>> personList =
                    new TypeReference<Map<String, JsonBackedAccount>>() {};
            this.jsonBackedAccounts = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeBean bean) {
        Assert.notNull(credential, "Credential cannot be null");
        Assert.notNull(bean, "PasswordChangeBean cannot be null");

        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;

        if (StringUtils.isBlank(c.getPassword()) || StringUtils.isBlank(bean.getPassword())) {
            LOGGER.error("Password cannot be blank");
            return false;
        }

        if (!StringUtils.equals(bean.getPassword(), bean.getConfirmedPassword())) {
            LOGGER.error("Password does not match and cannot be confirmed");
            return false;
        }

        final JsonBackedAccount account = this.jsonBackedAccounts.getOrDefault(c.getId(), null);
        if (account == null) {
            LOGGER.error("User account [{}] cannot be found", c.getId());
            return false;
        }

        account.setPassword(bean.getPassword());
        this.jsonBackedAccounts.put(c.getId(), account);
        return writeAccountToJsonResource();
    }

    private boolean writeAccountToJsonResource() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(this.jsonResource.getFile(), this.jsonBackedAccounts);
            readAccountsFromJsonResource();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public String findEmail(final String username) {
        final JsonBackedAccount account = this.jsonBackedAccounts.getOrDefault(username, null);
        return account == null ? null : account.getEmail();
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        final JsonBackedAccount account = this.jsonBackedAccounts.getOrDefault(username, null);
        if (account != null) {
            return account.getSecurityQuestions();
        }
        return new HashMap<>(0);
    }

    /**
     * The type Json backed account.
     */
    private static class JsonBackedAccount {
        private String email;
        private String password;
        private Map<String, String> securityQuestions = new HashMap<>();

        JsonBackedAccount() {
        }

        JsonBackedAccount(final String email, final String password, final Map<String, String> securityQuestions) {
            this.email = email;
            this.password = password;
            this.securityQuestions = securityQuestions;
        }

        public Map<String, String> getSecurityQuestions() {
            return securityQuestions;
        }

        public void setSecurityQuestions(final Map<String, String> securityQuestions) {
            this.securityQuestions = securityQuestions;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }
    }
}
