package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Marker interface for Services. Services are generally either remote
 * applications utilizing CAS or applications that principals wish to gain
 * access to. In most cases this will be some form of web application.
 *
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @since 3.0.0
 */
public interface Service extends Serializable {

    /**
     * Service attribute to keep track of various HTTP request properties.
     */
    String SERVICE_ATTRIBUTE_HTTP_REQUEST = "httpRequest";
    /**
     * Service attribute to keep track of the available cookies.
     */
    String SERVICE_ATTRIBUTE_COOKIES = "cookies";
    /**
     * Service attribute to keep track of the request headers.
     */
    String SERVICE_ATTRIBUTE_HEADERS = "headers";
    
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(Service.class);

    /**
     * Sets the principal.
     *
     * @param principal the new principal
     */
    default void setPrincipal(final String principal) {
    }

    /**
     * Sets the attributes.
     *
     * @param attributes the new attributes
     */
    void setAttributes(Map<String, Object> attributes);

    /**
     * Return the original url provided (as {@code service} or {@code targetService} request parameter).
     * Used to reconstruct the redirect url.
     *
     * @return the original url provided.
     */
    String getOriginalUrl();

    /**
     * Principal id.
     *
     * @return the unique id for the Principal
     */
    String getId();

    /**
     * Gets tenant.
     *
     * @return the tenant
     */
    String getTenant();

    /**
     * Sets tenant.
     *
     * @param tenant the tenant
     */
    void setTenant(String tenant);

    /**
     * Principal attributes.
     *
     * @return the map of configured attributes for this principal
     */
    default Map<String, Object> getAttributes() {
        return new LinkedHashMap<>();
    }

    /**
     * Gets attribute as requested type.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the attribute as
     */
    default <T> T getAttributeAs(final String name, final Class<T> clazz) {
        val attribute = getAttributes().get(name);
        if (attribute == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(attribute.getClass())) {
            throw new ClassCastException("Attribute " + name + '[' + attribute
                + " is of type " + attribute.getClass() + " when we were expecting " + clazz);
        }
        return (T) attribute;
    }

    /**
     * Gets first attribute value with requested type.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the first attribute
     */
    default <T> T getFirstAttribute(final String name, final Class<T> clazz) {
        val values = getAttributeAs(name, List.class);
        return values == null || values.isEmpty() ? null : clazz.cast(values.getFirst());
    }

    /**
     * Gets shortened id.
     *
     * @return the shortened id
     * @throws Exception the exception
     */
    @JsonIgnore
    default String getShortenedId() throws Exception {
        val urlBuilder = new URIBuilder(getId());

        var serviceId = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(urlBuilder.getScheme())) {
            serviceId += urlBuilder.getScheme().concat("://");
        }
        if (StringUtils.isNotBlank(urlBuilder.getHost())) {
            serviceId += urlBuilder.getHost();
        }
        if (urlBuilder.getPort() > 0) {
            serviceId += ":" + urlBuilder.getPort();
        }
        if (!urlBuilder.getPathSegments().isEmpty()) {
            serviceId += (StringUtils.isBlank(serviceId) ? StringUtils.EMPTY : '/') + urlBuilder.getPathSegments().getFirst();
        }
        if (urlBuilder.getPathSegments().size() >= 2) {
            serviceId += '/' + urlBuilder.getPathSegments().get(1);
        }
        if (!urlBuilder.getPathSegments().isEmpty()) {
            val lastSegment = urlBuilder.getPathSegments().getLast();
            if (!serviceId.contains(lastSegment)) {
                serviceId += '/' + lastSegment;
            }
        }
        return serviceId;
    }
}
