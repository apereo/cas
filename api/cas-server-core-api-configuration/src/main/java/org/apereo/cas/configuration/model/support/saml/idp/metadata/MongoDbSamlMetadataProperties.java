package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-mongo")
@Slf4j
@Getter
@Setter
public class MongoDbSamlMetadataProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -227092724742371662L;

    public MongoDbSamlMetadataProperties() {
        setCollection("cas-saml-metadata");
    }
}
