package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.core.util.MinimalPrettyPrinter;
import tools.jackson.databind.ObjectMapper;

/**
 * A proxy policy that only allows proxying to pgt urls
 * via a REST endpoint.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@ToString
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RestfulRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {

    @Serial
    private static final long serialVersionUID = -222069319543047324L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(true).build().toObjectMapper();

    private String endpoint;

    private Map<String, String> headers = new TreeMap<>();

    @JsonIgnore
    @Override
    public boolean isAllowedToProxy() {
        return true;
    }

    @Override
    public boolean isAllowedProxyCallbackUrl(final RegisteredService registeredService, final URL pgtUrl) {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            MAPPER.writer().with(new MinimalPrettyPrinter()).writeValue(writer, registeredService);
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .headers(headers)
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(endpoint))
                .entity(writer.toString())
                .parameters(CollectionUtils.wrap("pgtUrl", pgtUrl.toExternalForm()))
                .headers(CollectionUtils.wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .build();
            response = HttpUtils.execute(exec);
            return HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }
}
