package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JsonConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class JsonConsentProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 7079027843747126083L;
}
