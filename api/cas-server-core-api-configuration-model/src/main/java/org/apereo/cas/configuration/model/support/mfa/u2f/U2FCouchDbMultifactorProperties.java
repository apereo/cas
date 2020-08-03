package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FCouchDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f-couchdb")
@Getter
@Setter
@Accessors(chain = true)
public class U2FCouchDbMultifactorProperties extends BaseAsynchronousCouchDbProperties {

    private static final long serialVersionUID = 2751957521987245445L;

    public U2FCouchDbMultifactorProperties() {
        setDbName("u2f_multifactor");
    }
}
