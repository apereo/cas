package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.interrupt.RestfulInterruptProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link RestEndpointInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestEndpointInterruptInquirer extends BaseInterruptInquirer {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final RestfulInterruptProperties restProperties;

    /**
     * Ask the REST endpoint for the interrupt. The {@code Accept-Language} header of the current HTTP request, when
     * there is one on this thread, is passed along to the endpoint.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param service           the service
     * @param credential        the credential
     * @return the interrupt response, or none when the endpoint fails or answers with an error
     */
    @Override
    public InterruptResponse inquireInternal(final Authentication authentication,
                                             final RegisteredService registeredService,
                                             final Service service,
                                             final Credential credential) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            parameters.put("username", authentication.getPrincipal().getId());

            if (service != null) {
                parameters.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            if (registeredService != null) {
                parameters.put("registeredService", registeredService.getServiceId());
            }

            val headers = new HashMap<String, String>();
            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val acceptedLanguage = request != null ? request.getHeader(HttpHeaders.ACCEPT_LANGUAGE) : null;
            if (StringUtils.isNotBlank(acceptedLanguage)) {
                headers.put(HttpHeaders.ACCEPT_LANGUAGE, acceptedLanguage);
            }
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(restProperties.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(restProperties.getUrl())
                .parameters(parameters)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && !HttpStatusCode.valueOf(response.getCode()).is2xxSuccessful()) {
                LOGGER.warn("Interrupt endpoint [{}] returned status [{}]; no interrupt is produced", restProperties.getUrl(), response.getCode());
                return InterruptResponse.none();
            }
            if (response instanceof final HttpEntityContainer container && container.getEntity() != null) {
                try (val content = container.getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return MAPPER.readValue(JsonValue.readHjson(result).toString(), InterruptResponse.class);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return InterruptResponse.none();
    }
}
