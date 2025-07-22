package org.apereo.cas.syncope;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalProvisioningProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.Objects;

/**
 * This is {@link SyncopePrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SyncopePrincipalProvisioner implements PrincipalProvisioner {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final SyncopePrincipalProvisioningProperties properties;

    @Override
    public boolean provision(final Principal principal, final Credential credential) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.info("Searching Syncope to find [{}]", principal.getId());
            val userList = SyncopeUtils.syncopeUserSearch(properties, principal.getId());
            if (userList.isEmpty()) {
                return createUserResource(principal, credential);
            }
            return userList
                .stream()
                .allMatch(Unchecked.predicate(__ -> updateUserResource(principal)));
        });
    }

    protected boolean updateUserResource(final Principal principal) throws Exception {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = StringUtils.appendIfMissing(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()), "/rest/users/" + principal.getId());
            val headers = CollectionUtils.<String, String>wrap(SyncopeUtils.SYNCOPE_HEADER_DOMAIN, properties.getDomain(),
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());

            val entity = MAPPER.writeValueAsString(SyncopeUtils.convertToUserUpdateEntity(principal, getSyncopeRealm(principal)));
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.PATCH)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .headers(headers)
                .entity(entity)
                .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getReasonPhrase());
            val result = EntityUtils.toString(((HttpEntityContainer) response).getEntity());
            LOGGER.debug("Received response payload as [{}]", result);
            return HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
        } finally {
            HttpUtils.close(response);
        }
    }

    protected String getSyncopeRealm(final Principal principal) {
        return CollectionUtils.firstElement(principal.getAttributes().get("realm"))
            .map(Object::toString)
            .orElseGet(properties::getRealm);
    }

    protected boolean createUserResource(final Principal principal, final Credential credential) throws Exception {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = StringUtils.appendIfMissing(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()), "/rest/users");
            val headers = CollectionUtils.<String, String>wrap(SyncopeUtils.SYNCOPE_HEADER_DOMAIN, properties.getDomain(),
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());

            val entity = MAPPER.writeValueAsString(SyncopeUtils.convertToUserCreateEntity(principal, getSyncopeRealm(principal)));

            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .headers(headers)
                .entity(entity)
                .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getReasonPhrase());
            val result = EntityUtils.toString(((HttpEntityContainer) response).getEntity());
            LOGGER.debug("Received response payload as [{}]", result);

            return HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
        } finally {
            HttpUtils.close(response);
        }
    }

}
