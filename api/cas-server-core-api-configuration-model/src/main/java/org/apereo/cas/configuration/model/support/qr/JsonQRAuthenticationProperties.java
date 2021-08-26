package org.apereo.cas.configuration.model.support.qr;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JsonQRAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-qr-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class JsonQRAuthenticationProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 7179027843747126083L;
}
