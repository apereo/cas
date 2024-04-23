package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link GroovyConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class GroovyConsentProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 7079027843747126083L;
}
