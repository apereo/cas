package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchDbConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-couchdb")
@Getter
@Setter
@Accessors(chain = true)
public class CouchDbConsentProperties extends BaseCouchDbProperties {
    private static final long serialVersionUID = 8184753250455916462L;

    public CouchDbConsentProperties() {
        this.setDbName("consent");
    }
}
