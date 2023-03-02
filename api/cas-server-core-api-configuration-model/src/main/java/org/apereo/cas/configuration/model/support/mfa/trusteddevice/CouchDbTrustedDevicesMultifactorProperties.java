package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CouchDbTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 7
 */
@RequiresModule(name = "cas-server-support-trusted-mfa-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CouchDbTrustedDevicesMultifactorProperties")
@Deprecated(since = "7.0.0")
public class CouchDbTrustedDevicesMultifactorProperties extends BaseCouchDbProperties {

    @Serial
    private static final long serialVersionUID = 5887850351177564308L;

    public CouchDbTrustedDevicesMultifactorProperties() {
        setDbName("trusted_devices_multifactor");
    }
}
