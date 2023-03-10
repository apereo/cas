package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CouchDbGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 7
 */
@RequiresModule(name = "cas-server-support-gauth-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CouchDbGoogleAuthenticatorMultifactorProperties")
@Deprecated(since = "7.0.0")
public class CouchDbGoogleAuthenticatorMultifactorProperties extends BaseCouchDbProperties {

    @Serial
    private static final long serialVersionUID = -6260683393319585262L;

    public CouchDbGoogleAuthenticatorMultifactorProperties() {
        setDbName("gauth_multifactor");
    }
}
