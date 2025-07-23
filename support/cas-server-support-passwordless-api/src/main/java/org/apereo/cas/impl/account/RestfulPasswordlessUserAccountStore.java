package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationRestAccountsProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
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

    protected final PasswordlessAuthenticationRestAccountsProperties restProperties;

    protected final ConfigurableApplicationContext applicationContext;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            parameters.put("username", request.getUsername());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(Strings.CI.appendIfMissing(restProperties.getUrl(), "/").concat(request.getUsername()))
                .parameters(parameters)
                .headers(restProperties.getHeaders())
                .build();
            response = HttpUtils.execute(exec);

            if (response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful() && ((HttpEntityContainer) response).getEntity() != null) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val account = MAPPER.readValue(JsonValue.readHjson(result).toString(), PasswordlessUserAccount.class);
                    return Optional.ofNullable(account);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
