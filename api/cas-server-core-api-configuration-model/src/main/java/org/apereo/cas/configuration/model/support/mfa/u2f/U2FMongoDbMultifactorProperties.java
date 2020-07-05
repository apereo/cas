package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FMongoDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f-mongo")
@Getter
@Setter
@Accessors(chain = true)
public class U2FMongoDbMultifactorProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -7963843335569634144L;

    public U2FMongoDbMultifactorProperties() {
        setCollection("CasMongoDbU2FRepository");
    }
}
