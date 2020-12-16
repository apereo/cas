package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchDbTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-trusted-mfa-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CouchDbTrustedDevicesMultifactorProperties")
public class CouchDbTrustedDevicesMultifactorProperties extends BaseCouchDbProperties {

    private static final long serialVersionUID = 5887850351177564308L;

    public CouchDbTrustedDevicesMultifactorProperties() {
        setDbName("trusted_devices_multifactor");
    }
}
