package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.simple.RestfulCasSimpleMultifactorAuthenticationTokenProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.JsonUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import javax.security.auth.login.FailedLoginException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link RestfulCasSimpleMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class RestfulCasSimpleMultifactorAuthenticationService extends BaseCasSimpleMultifactorAuthenticationService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(true).build().toObjectMapper();

    private final RestfulCasSimpleMultifactorAuthenticationTokenProperties properties;

    private final TicketFactory ticketFactory;

    public RestfulCasSimpleMultifactorAuthenticationService(final TicketRegistry ticketRegistry,
                                                            final RestfulCasSimpleMultifactorAuthenticationTokenProperties properties,
                                                            final TicketFactory ticketFactory) {
        super(ticketRegistry);
        this.properties = properties;
        this.ticketFactory = ticketFactory;
    }

    @Override
    public CasSimpleMultifactorAuthenticationTicket generate(final Principal principal, final Service service) throws Exception {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, principal);

            val parameters = new LinkedHashMap<String, String>();
            Optional.ofNullable(service).ifPresent(s -> parameters.put("service", s.getId()));

            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .headers(headers)
                .url(Strings.CI.appendIfMissing(properties.getUrl(), "/").concat("new"))
                .entity(writer.toString())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory) ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
                    LOGGER.debug("Multifactor authentication token received is [{}]", result);
                    val token = mfaFactory.create(result, service, CollectionUtils.wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
                    LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token.getId(), service);
                    return token;
                }
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

            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .headers(headers)
                .url(properties.getUrl())
                .entity(writer.toString())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
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
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .headers(headers)
                .url(Strings.CI.appendIfMissing(properties.getUrl(), "/").concat(credential.getToken()))
                .entity(writer.toString())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return MAPPER.readValue(JsonValue.readHjson(result).toString(), Principal.class);
                }
            }
            throw new FailedLoginException("Unable to validate multifactor credential with status " + statusCode);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public Principal fetch(final CasSimpleMultifactorTokenCredential tokenCredential) throws Exception {
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .headers(headers)
                .url(Strings.CI.appendIfMissing(properties.getUrl(), "/").concat(tokenCredential.getToken()))
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return MAPPER.readValue(JsonValue.readHjson(result).toString(), Principal.class);
                }
            }
            throw new FailedLoginException("Unable to validate multifactor credential with status " + statusCode);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void update(final Principal principal, final Map<String, Object> attributes) {
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.PUT)
                .headers(headers)
                .entity(JsonUtils.render(MAPPER, Map.of("principal", principal, "attributes", attributes)))
                .url(properties.getUrl())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .build();
            response = HttpUtils.execute(exec);
            Assert.isTrue(HttpStatus.valueOf(response.getCode()).is2xxSuccessful(), "Unable to update principal");
        } finally {
            HttpUtils.close(response);
        }
    }
}
