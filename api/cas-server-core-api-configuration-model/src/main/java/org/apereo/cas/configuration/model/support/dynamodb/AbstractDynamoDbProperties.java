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
     * Billing mode specifies how you are charged for read and write throughput
     * and how you manage capacity.
     */
    private BillingMode billingMode = BillingMode.PROVISIONED;

    /**
     * Indicates that the database instance is local to the deployment
     * that does not require or use any credentials or other configuration
     * other than host and region. This is mostly used during development
     * and testing.
     */
    private boolean localInstance;

    public enum BillingMode {
        /**
         * Provisioned mode means that you specify the number of reads
         * and writes per second that you expect your
         * application to use.
         * Provisioned mode is a good option if any of the following are true:
         *
         * <ul>
         *  <li>You have predictable application traffic.</li>
         *  <li>You run applications whose traffic is consistent or ramps gradually.</li>
         *  <li>You can forecast capacity requirements to control costs.</li>
         * </ul>
         * You can use auto scaling to automatically adjust
         * capacity based on the specified utilization rate
         * to ensure application performance while reducing costs.
         */
        PROVISIONED,

        /**
         * Pay-per-request or on-demand billing means that you're charged for only the read/write
         * requests that you use.
         * On-demand mode is a good option if any of the following are true:
         * <ul>
         *     <li>You create new tables with unknown workloads.</li>
         *     <li>You have unpredictable application traffic.</li>
         *     <li>You prefer the ease of paying for only what you use.</li>
         * </ul>
         * Tables using on-demand mode support all DynamoDB
         * features (such as encryption at rest, point-in-time recovery, global
         * tables, and so on) with the exception of auto scaling,
         * which is not applicable with this mode.
         */
        PAY_PER_REQUEST
    }
}
