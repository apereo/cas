package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.configuration.model.support.account.provision.RestfulAccountManagementRegistrationProvisioningProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

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

    private final RestfulAccountManagementRegistrationProvisioningProperties properties;

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) throws Exception {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.putAll(properties.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(properties.getUrl())
                .headers(headers)
                .entity(MAPPER.writeValueAsString(request))
                .build();
            response = HttpUtils.execute(exec);
            if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                val entity = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val success = AccountRegistrationResponse.success();
                Arrays.stream(response.getAllHeaders())
                    .forEach(header -> success.putProperty(header.getName(), header.getValue()));
                FunctionUtils.doIf(StringUtils.isNotBlank(entity),
                    value -> success.putProperty("entity", value)).accept(StringUtils.defaultString(entity));
                success.putProperty("status", response.getStatusLine().getStatusCode());
                success.putProperty("entity", StringUtils.defaultString(entity));
                return success;
            }
            val details = CollectionUtils.wrap("status", response.getStatusLine().getStatusCode(),
                "reason", response.getStatusLine().getReasonPhrase());
            Arrays.stream(response.getAllHeaders())
                .forEach(header -> details.put(header.getName(), header.getValue()));
            return new AccountRegistrationResponse(details);
        } finally {
            HttpUtils.close(response);
        }
    }
}
