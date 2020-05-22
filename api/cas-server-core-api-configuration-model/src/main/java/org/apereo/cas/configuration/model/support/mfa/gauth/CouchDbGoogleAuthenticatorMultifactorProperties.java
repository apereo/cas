package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchDbGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth-couchdb")
@Getter
@Setter
@Accessors(chain = true)
public class CouchDbGoogleAuthenticatorMultifactorProperties extends BaseCouchDbProperties {

    private static final long serialVersionUID = -6260683393319585262L;

    public CouchDbGoogleAuthenticatorMultifactorProperties() {
        setDbName("gauth_multifactor");
    }
}
