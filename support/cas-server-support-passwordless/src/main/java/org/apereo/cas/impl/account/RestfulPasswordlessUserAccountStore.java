package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationRestAccountsProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;

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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final PasswordlessAuthenticationRestAccountsProperties restProperties;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final String username) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", username);

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase().trim()))
                .url(restProperties.getUrl())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);

            if (response != null && response.getEntity() != null) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val account = MAPPER.readValue(JsonValue.readHjson(result).toString(), PasswordlessUserAccount.class);
                return Optional.ofNullable(account);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
