package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.simple.RestfulCasSimpleMultifactorAuthenticationTokenProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.security.auth.login.FailedLoginException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * This is {@link RestfulCasSimpleMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulCasSimpleMultifactorAuthenticationService implements CasSimpleMultifactorAuthenticationService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(true).build().toObjectMapper();

    private final RestfulCasSimpleMultifactorAuthenticationTokenProperties properties;

    private final TicketFactory ticketFactory;

    @Override
    public CasSimpleMultifactorAuthenticationTicket generate(final Principal principal, final Service service) throws Exception {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, principal);

            val parameters = new LinkedHashMap<String, String>();
            Optional.ofNullable(service).ifPresent(s -> parameters.put("service", s.getId()));

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .headers(properties.getHeaders())
                .url(StringUtils.appendIfMissing(properties.getUrl(), "/").concat("new"))
                .entity(writer.toString())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .parameters(parameters)
                .headers(CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(((HttpEntityContainer) response).getEntity().getContent(), StandardCharsets.UTF_8);
                val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory) ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
                LOGGER.debug("Multifactor authentication token received is [{}]", result);
                val token = mfaFactory.create(result, service, CollectionUtils.wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
                LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token.getId(), service);
                return token;
            }
            throw new FailedLoginException("Unable to validate multifactor credential with status " + statusCode);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void store(final CasSimpleMultifactorAuthenticationTicket token) throws Exception {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, token);
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .headers(properties.getHeaders())
                .url(properties.getUrl())
                .entity(writer.toString())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .headers(CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            if (HttpStatus.valueOf(statusCode).isError()) {
                throw new FailedLoginException("Unable to validate multifactor credential with status " + statusCode);
            }
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public Principal validate(final Principal resolvedPrincipal,
                              final CasSimpleMultifactorTokenCredential credential) throws Exception {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, resolvedPrincipal);
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .headers(properties.getHeaders())
                .url(StringUtils.appendIfMissing(properties.getUrl(), "/").concat(credential.getToken()))
                .entity(writer.toString())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .headers(CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(((HttpEntityContainer) response).getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), Principal.class);
            }
            throw new FailedLoginException("Unable to validate multifactor credential with status " + statusCode);
        } finally {
            HttpUtils.close(response);
        }
    }
}
