package org.apereo.cas.configuration.model.support.qr;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
public class QRAuthenticationProperties implements Serializable {
    @Serial
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
