package org.apereo.cas.configuration.model.core.config.cloud;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SpringCloudConfigurationProperties}. This class is only designed here
 * to allow the configuration binding logic to recognize the settings. In actuality, the fields
 * listed here are not used directly as they are directly accessed and fetched via the runtime
 * environment to bootstrap cas settings in form of a property source locator, etc.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class SpringCloudConfigurationProperties implements Serializable {
    private static final long serialVersionUID = -2749293768878152908L;

    /**
     * Config config settings.
     */
    private Cloud cloud = new Cloud();

    @Getter
    @Setter
    public static class Cloud implements Serializable {
        private static final long serialVersionUID = -6326706651416825269L;

        /**
         * MongoDb config settings.
         */
        private MongoDb mongo = new MongoDb();

        /**
         * Jdbc config settings.
         */
        private Jdbc jdbc = new Jdbc();

        /**
         * REST config settings.
         */
        private Rest rest = new Rest();

        /**
         * AWS config settings.
         */
        private AmazonWebServicesConfiguration aws = new AmazonWebServicesConfiguration();

        /**
         * AWS DynamoDb config settings.
         */
        private AmazonDynamoDb dynamoDb = new AmazonDynamoDb();
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-mongo")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MongoDb implements Serializable {
        private static final long serialVersionUID = -6509143371334754469L;

        /**
         * Mongodb URI.
         */
        @RequiredProperty
        private String uri;
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-rest")
    @Getter
    @Accessors(chain = true)
    @Setter
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -4509143371334754469L;
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-jdbc")
    @Getter
    @Accessors(chain = true)
    @Setter
    public static class Jdbc implements Serializable {
        private static final long serialVersionUID = -7575240387340025345L;

        /**
         * SQL statement.
         */
        private String sql;

        /**
         * Database url.
         */
        private String url;

        /**
         * Database user.
         */
        private String user;

        /**
         * Database password.
         */
        private String password;

        /**
         * Driver class name.
         */
        private String driverClass;
    }

    @RequiresModule(name = "cas-server-support-aws")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AmazonWebServicesConfiguration implements Serializable {
        private static final long serialVersionUID = -124404249388429120L;

        /**
         * AWS secrets manager settings.
         */
        private AmazonSecretsManager secretsManager = new AmazonSecretsManager();

        /**
         * AWS dynamo db settings.
         */
        private AmazonDynamoDb dynamoDb = new AmazonDynamoDb();

        /**
         * AWS S3 settings.
         */
        private AmazonS3 s3 = new AmazonS3();

        /**
         * AWS SSM settings.
         */
        private AmazonSystemsManagerParameterStore ssm = new AmazonSystemsManagerParameterStore();
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-aws-secretsmanager")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AmazonSecretsManager extends BaseAmazonWebServicesProperties {
        private static final long serialVersionUID = -124404249387429120L;
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-aws-ssm")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AmazonSystemsManagerParameterStore extends BaseAmazonWebServicesProperties {
        private static final long serialVersionUID = -224404249387429120L;
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-aws-s3")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AmazonS3 extends BaseAmazonWebServicesProperties {
        private static final long serialVersionUID = -124404249387429120L;

        /**
         * Bucket name that holds the settings.
         */
        private String bucketName;
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-dynamodb")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AmazonDynamoDb extends AbstractDynamoDbProperties {
        private static final long serialVersionUID = -123404249388429120L;
    }

}
