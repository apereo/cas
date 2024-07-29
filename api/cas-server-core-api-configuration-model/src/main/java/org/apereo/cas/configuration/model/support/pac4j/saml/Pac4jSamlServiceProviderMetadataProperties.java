package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Pac4jSamlServiceProviderMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class Pac4jSamlServiceProviderMetadataProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -552809796533384951L;

    /**
     * Location of the SP metadata to use and generate
     * on the file system. If the metadata file already exists,
     * it will be ignored and reused.
     */
    @NestedConfigurationProperty
    private Pac4jSamlServiceProviderMetadataFileSystemProperties fileSystem = new Pac4jSamlServiceProviderMetadataFileSystemProperties();

    /**
     * Location of the SP metadata to use and generate
     * using a MongoDb instance.
     */
    @NestedConfigurationProperty
    private Pac4jSamlServiceProviderMetadataMongoDbProperties mongo = new Pac4jSamlServiceProviderMetadataMongoDbProperties();

    /**
     * Location of the SP metadata to use and generate
     * using a relational database (i.e. MySQL) instance.
     */
    @NestedConfigurationProperty
    private Pac4jSamlServiceProviderMetadataJdbcProperties jdbc = new Pac4jSamlServiceProviderMetadataJdbcProperties();
    
    /**
     * Properties pertaining to amazon s3 to manage SP metadata.
     */
    @NestedConfigurationProperty
    private Pac4jSamlServiceProviderMetadataAmazonS3Properties amazonS3 = new Pac4jSamlServiceProviderMetadataAmazonS3Properties();
}
