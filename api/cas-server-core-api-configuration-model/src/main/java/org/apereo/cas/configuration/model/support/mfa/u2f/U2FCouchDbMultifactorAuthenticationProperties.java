package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FCouchDbMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FCouchDbMultifactorAuthenticationProperties")
public class U2FCouchDbMultifactorAuthenticationProperties extends BaseAsynchronousCouchDbProperties {

    private static final long serialVersionUID = 2751957521987245445L;

    public U2FCouchDbMultifactorAuthenticationProperties() {
        setDbName("u2f_multifactor");
    }
}
