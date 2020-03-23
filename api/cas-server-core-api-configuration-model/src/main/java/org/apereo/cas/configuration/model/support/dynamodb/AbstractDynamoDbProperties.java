package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AbstractDynamoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-core")
@Getter
@Setter
@Accessors(chain = true)
public abstract class AbstractDynamoDbProperties extends BaseAmazonWebServicesProperties {
    private static final long serialVersionUID = -8349917272283787550L;

    /**
     * Flag that indicates whether to drop tables on start up.
     */
    private boolean dropTablesOnStartup;

    /**
     * Flag that indicates whether to prevent CAS from creating tables.
     */
    private boolean preventTableCreationOnStartup;

    /**
     * Time offset.
     */
    private int timeOffset;

    /**
     * Read capacity.
     */
    private long readCapacity = 10;

    /**
     * Write capacity.
     */
    private long writeCapacity = 10;

    /**
     * Indicates that the database instance is local to the deployment
     * that does not require or use any credentials or other configuration
     * other than host and region. This is mostly used during development
     * and testing.
     */
    private boolean localInstance;

}
