package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static final long serialVersionUID = -1020542741768471305L;

    /**
     * Whether invalid metadata should eagerly fail quickly on startup
     * once the resource is parsed.
     */
    private boolean failFast = true;

    /**
     * Whether valid metadata is required.
     */
    private boolean requireValidMetadata = true;

    /**
     * Forcefully download and fetch metadata files
     * form URL sources and disregard any cached copies
     * of the metadata.
     */
    private boolean forceMetadataRefresh = true;

    /**
     * How long should metadata be cached in minutes.
     */
    private long cacheExpirationMinutes = TimeUnit.DAYS.toMinutes(1);

    /**
     * Directory location of SAML metadata and signing/encryption keys.
     * This directory will be used to hold the configuration files.
     */
    @RequiredProperty
    private String location = "file:/etc/cas/saml";

    /**
     * Properties pertaining to mongo db saml metadata resolvers.
     */
    @NestedConfigurationProperty
    private MongoDbSamlMetadataProperties mongo = new MongoDbSamlMetadataProperties();

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
     * Properties pertaining to CouchDB metadata resolution.
     */
    @NestedConfigurationProperty
    private CouchDbSamlMetadataProperties couchDb = new CouchDbSamlMetadataProperties();

    /**
     * Algorithm name to use when generating private key.
     */
    private String privateKeyAlgName = "RSA";

    /**
     * Basic auth username in case the metadata instance is connecting to an MDQ server.
     */
    private String basicAuthnUsername;

    /**
     * Basic auth password in case the metadata instance is connecting to an MDQ server.
     */
    private String basicAuthnPassword;

    /**
     * Supported content types in case the metadata instance is connecting to an MDQ server.
     */
    private List<String> supportedContentTypes = new ArrayList<>(0);

    public SamlIdPMetadataProperties() {
        supportedContentTypes.add("application/xml");
        supportedContentTypes.add("text/xml");
    }
}
