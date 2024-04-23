package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link Pac4jSamlServiceProviderMetadataMongoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jSamlServiceProviderMetadataMongoDbProperties")
public class Pac4jSamlServiceProviderMetadataMongoDbProperties extends SingleCollectionMongoDbProperties {
    @Serial
    private static final long serialVersionUID = -5114734720383722585L;
}
