package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RestfulConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-rest")
@Getter
@Setter
@Accessors(chain = true)

public class RestfulConsentProperties extends BaseRestEndpointProperties {

    @Serial
    private static final long serialVersionUID = -6909617495470495341L;
}
