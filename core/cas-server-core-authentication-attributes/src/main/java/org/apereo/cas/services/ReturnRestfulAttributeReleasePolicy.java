package org.apereo.cas.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Return a collection of allowed attributes for the principal based on an external REST endpoint.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ReturnRestfulAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnRestfulAttributeReleasePolicy.class);

    private String endpoint;

    /**
     * Instantiates a new Return mapped attribute release policy.
     */
    public ReturnRestfulAttributeReleasePolicy() {
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attributes,
                                                        final RegisteredService service) {
        try (StringWriter writer = new StringWriter()) {
            MAPPER.writer(new MinimalPrettyPrinter()).writeValue(writer, attributes);
            final HttpResponse response = HttpUtils.executePost(this.endpoint, writer.toString(),
                    CollectionUtils.wrap("principal", principal.getId(), "service", service.getServiceId()));
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return MAPPER.readValue(response.getEntity().getContent(),
                        new TypeReference<Map<String, Object>>() {
                        });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ReturnRestfulAttributeReleasePolicy rhs = (ReturnRestfulAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.endpoint, rhs.endpoint)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.endpoint)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("endpoint", this.endpoint)
                .toString();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }
}
