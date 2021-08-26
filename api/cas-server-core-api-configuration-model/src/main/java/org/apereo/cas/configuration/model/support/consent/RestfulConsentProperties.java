package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("RestfulConsentProperties")
public class RestfulConsentProperties extends BaseRestEndpointProperties {

    private static final long serialVersionUID = -6909617495470495341L;
}
