package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.Serial;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Return a collection of allowed attributes for the principal based on an external REST endpoint.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ReturnRestfulAttributeReleasePolicy extends BaseMappedAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -6249488544306639050L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).build().toObjectMapper();

    private String method = "GET";

    private String endpoint;

    private Map<String, String> headers = new TreeMap<>();

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) {
        HttpResponse response = null;
        try (val writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, attributes);
            headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.valueOf(this.method))
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(endpoint))
                .parameters(CollectionUtils.wrap("principal", context.getPrincipal().getId(),
                    "service", context.getRegisteredService().getServiceId()))
                .entity(writer.toString())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && HttpStatus.resolve(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Policy response received: [{}]", result);
                val returnedAttributes = MAPPER.readValue(result, new TypeReference<Map<String, List<Object>>>() {
                });
                return FunctionUtils.doIf(getAllowedAttributes().isEmpty(),
                        () -> returnedAttributes,
                        () -> authorizeMappedAttributes(context, returnedAttributes))
                    .get();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashMap<>(0);
    }

}
