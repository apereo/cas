package org.apereo.cas.configuration.model.support.qr;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link QRAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-qr-authentication")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("QRAuthenticationProperties")
public class QRAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 8726382874579042117L;

    /**
     * Configure allowed {@code Origin} header values. This check is mostly designed for
     * browser clients. There is nothing preventing other types of client to modify the
     * {@code Origin} header value.
     *
     * <p>When SockJS is enabled and origins are restricted, transport types that do not
     * allow to check request origin (Iframe based transports) are disabled.
     * As a consequence, IE 6 to 9 are not supported when origins are restricted.
     *
     * <p>Each provided allowed origin must start by "http://", "https://" or be "*"
     * (means that all origins are allowed). By default, only same origin requests are
     * allowed (empty list).
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Track registered devices in a repository backed by a JSON resource.
     */
    @NestedConfigurationProperty
    private JsonQRAuthenticationProperties json = new JsonQRAuthenticationProperties();
}
