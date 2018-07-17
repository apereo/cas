package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpStatus;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

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
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ReturnRestfulAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private String endpoint;

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attributes, final RegisteredService service) {
        try (val writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, attributes);
            val response = HttpUtils.executePost(this.endpoint, writer.toString(), CollectionUtils.wrap("principal", principal.getId(), "service", service.getServiceId()));
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return MAPPER.readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

}
