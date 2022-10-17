package org.apereo.cas.syncope;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAccountManagementRegistrationProvisioningProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SyncopeAccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SyncopeAccountRegistrationProvisioner implements AccountRegistrationProvisioner {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final SyncopeAccountManagementRegistrationProvisioningProperties properties;

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) {
        val response = new AccountRegistrationResponse();
        Splitter.on(",").splitToList(properties.getDomain())
            .stream()
            .map(Unchecked.function(domain -> submitRequest(request, domain)))
            .forEach(result -> {
                response.putProperty(result.getProperty("domain", String.class), result);
                response.putProperty("success", result.isSuccess());
            });
        return response;
    }

    private AccountRegistrationResponse submitRequest(final AccountRegistrationRequest request,
                                                      final String domain) throws Exception {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = StringUtils.appendIfMissing(properties.getUrl(), "/rest/users");
            val headers = CollectionUtils.<String, String>wrap("X-Syncope-Domain", domain,
                "Accept", MediaType.APPLICATION_JSON_VALUE,
                "Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());

            val entity = MAPPER.writeValueAsString(SyncopeUtils.convertToUserCreateEntity(request.getProperties(),
                new UsernamePasswordCredential(request.getUsername(), request.getPassword()), getSyncopeRealm(request)));
              
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .headers(headers)
                .entity(entity)
                .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());
            if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).isError()) {
                val result = EntityUtils.toString(response.getEntity());
                LOGGER.debug("Received user object as [{}]", result);
                val responseJson = MAPPER.readValue(result, new TypeReference<Map<String, Object>>() {
                });
                return AccountRegistrationResponse.success()
                    .putProperty("domain", domain)
                    .putProperty("entity", responseJson.get("entity"))
                    .putProperty("propagationStatuses", responseJson.get("propagationStatuses"));
            }
        } finally {
            HttpUtils.close(response);
        }
        return AccountRegistrationResponse.failure().putProperty("domain", domain);
    }

    protected String getSyncopeRealm(final AccountRegistrationRequest request) {
        return StringUtils.defaultString(request.getProperty("realm", String.class), properties.getRealm());
    }
}
