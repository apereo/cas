package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.configuration.model.support.account.provision.RestfulAccountManagementRegistrationProvisioningProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is {@link RestfulAccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class RestfulAccountRegistrationProvisioner implements AccountRegistrationProvisioner {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .build()
        .toObjectMapper();

    private final HttpClient httpClient;
    private final RestfulAccountManagementRegistrationProvisioningProperties properties;

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) throws Exception {
        val response = new AtomicReference<HttpResponse>();
        try {
            response.set(executeRequest(request));
            return FunctionUtils.doIfNull(response.get(),
                    AccountRegistrationResponse::new,
                    () -> buildAccountRegistrationResponse(response.get()))
                .get();
        } finally {
            HttpUtils.close(response.get());
        }
    }

    protected AccountRegistrationResponse buildAccountRegistrationResponse(final HttpResponse response) {
        return FunctionUtils.doUnchecked(() -> {
            if (HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val entity = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val success = AccountRegistrationResponse.success();
                    Arrays.stream(response.getHeaders())
                            .forEach(header -> success.putProperty(header.getName(), header.getValue()));
                    FunctionUtils.doIf(StringUtils.isNotBlank(entity),
                            value -> success.putProperty("entity", value)).accept(StringUtils.defaultString(entity));
                    success.putProperty("status", response.getCode());
                    success.putProperty("entity", StringUtils.defaultString(entity));
                    return success;
                }
            }
            val details = CollectionUtils.wrap("status", response.getCode(),
                "reason", response.getReasonPhrase());
            Arrays.stream(response.getHeaders())
                .forEach(header -> details.put(header.getName(), header.getValue()));
            return new AccountRegistrationResponse(details);
        });
    }

    protected HttpResponse executeRequest(final AccountRegistrationRequest request) throws Exception {
        val headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.putAll(properties.getHeaders());

        val exec = HttpExecutionRequest.builder()
            .basicAuthPassword(properties.getBasicAuthPassword())
            .basicAuthUsername(properties.getBasicAuthUsername())
            .method(HttpMethod.POST)
            .url(properties.getUrl())
            .headers(headers)
            .entity(MAPPER.writeValueAsString(request))
            .httpClient(httpClient)
            .build();
        return HttpUtils.execute(exec);
    }
}
