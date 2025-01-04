package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SamlIdPMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)

public class SamlIdPMetadataProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1020542741768471305L;

    /**
     * Core and common settings related to saml2 metadata management.
     */
    @NestedConfigurationProperty
    private CoreSamlMetadataProperties core = new CoreSamlMetadataProperties();

    /**
     * Settings related to saml2 metadata management,
     * when fetching or handling metadata over http endpoints
     * from URL resources.
     */
    @NestedConfigurationProperty
    private HttpSamlMetadataProperties http = new HttpSamlMetadataProperties();

    /**
     * Settings related to saml2 metadata management,
     * when fetching or handling metadata using the file system.
     */
    @NestedConfigurationProperty
    private FileSystemSamlMetadataProperties fileSystem = new FileSystemSamlMetadataProperties();

    /**
     * Properties pertaining to mongo db saml metadata resolvers.
     */
    @NestedConfigurationProperty
    private MongoDbSamlMetadataProperties mongo = new MongoDbSamlMetadataProperties();

    /**
     * Properties pertaining to dynamo-db saml metadata resolvers.
     */
    @NestedConfigurationProperty
    private DynamoDbSamlMetadataProperties dynamoDb = new DynamoDbSamlMetadataProperties();

    /**
     * Properties pertaining to redis saml metadata resolvers.
     */
    @NestedConfigurationProperty
    private RedisSamlMetadataProperties redis = new RedisSamlMetadataProperties();

    /**
     * Properties pertaining to git saml metadata resolvers.
     */
    @NestedConfigurationProperty
    private GitSamlMetadataProperties git = new GitSamlMetadataProperties();

    /**
     * Properties pertaining to jpa metadata resolution.
     */
    @NestedConfigurationProperty
    private JpaSamlMetadataProperties jpa = new JpaSamlMetadataProperties();

    /**
     * Properties pertaining to REST metadata resolution.
     */
    @NestedConfigurationProperty
    private RestSamlMetadataProperties rest = new RestSamlMetadataProperties();

    /**
     * Properties pertaining to AWS S3 metadata resolution.
     */
    @NestedConfigurationProperty
    private AmazonS3SamlMetadataProperties amazonS3 = new AmazonS3SamlMetadataProperties();

    /**
     * Metadata management settings via MDQ protocol.
     */
    @NestedConfigurationProperty
    private MDQSamlMetadataProperties mdq = new MDQSamlMetadataProperties();
}
