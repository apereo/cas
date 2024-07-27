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
    void setAttributes(Map<String, List<Object>> attributes);

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
     * Principal attributes.
     *
     * @return the map of configured attributes for this principal
     */
    default Map<String, List<Object>> getAttributes() {
        return new LinkedHashMap<>(0);
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
