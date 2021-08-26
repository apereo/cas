package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WebAuthnMongoDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-webauthn-mongo")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("WebAuthnMongoDbMultifactorProperties")
public class WebAuthnMongoDbMultifactorProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = 6876845341227039713L;

    public WebAuthnMongoDbMultifactorProperties() {
        setCollection("MongoDbWebAuthnRepository");
    }

}
