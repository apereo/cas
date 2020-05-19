package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationRestAccountsProperties;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link RestfulPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final PasswordlessAuthenticationRestAccountsProperties restProperties;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final String username) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", username);

            response = HttpUtils.execute(restProperties.getUrl(), restProperties.getMethod(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>(0));
            if (response != null && response.getEntity() != null) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val account = MAPPER.readValue(JsonValue.readHjson(result).toString(), PasswordlessUserAccount.class);
                return Optional.ofNullable(account);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
