package org.apereo.cas.configuration.model.support.ignite;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link IgniteProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-ignite-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class IgniteProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -5259465262649559156L;

    /**
     * A comma-separated list of address:port pairs representing the entry points
     * for the cluster discovery.
     * <p>
     * This maps to the {@code netClusterNodes} configuration in the Node Finder.
     * When this node starts, it will attempt to contact these addresses to find
     * existing peers and join the topology.
     * </p>
     */
    @RequiredProperty
    private List<String> igniteServers = Stream.of("localhost:47500").toList();

    /**
     * The unique identifier for this specific Ignite node instance.
     * <p>
     * This name is used for:
     * <ul>
     * <li>Log identification</li>
     * <li>Consistent Hashing (topology placement)</li>
     * <li>Raft voting identity</li>
     * <li>MetaStorage leadership election</li>
     * </ul>
     * <strong>Requirement:</strong> This must be unique across all nodes in the cluster.
     * Duplicate names will result in the node being rejected during the join phase.
     */
    private String nodeName = "cas-ignite-node-" + UUID.randomUUID();

    /**
     * The human-readable logical name of the cluster.
     * <p>
     * This value is passed to the cluster builder
     * during the initialization phase (see {@link #initializeCluster}). It serves as the
     * identifier for the cluster topology management group (CMG).
     * </p>
     * <p>
     * <strong>Note:</strong> This is only strictly required if {@link #initializeCluster} is true,
     * as joining nodes will inherit the cluster name from the active topology.
     * </p>
     */
    private String clusterName = "cas-ignite-cluster";

    /**
     * The local network port that this Ignite node will bind to and listen on.
     * This is a unified port used for both node discovery and data communication.
     * If you are running multiple nodes on the same physical machine (or the same container
     * network), each node <strong>must</strong> have a unique port to avoid {@code BindException}.
     */
    private int port = 47500;

    /**
     * A flag indicating whether this specific node instance should attempt to
     * initialize the cluster topology upon startup.
     * <p>
     * A cluster is not operational until {@code initializeCluster} is set to {@code true}
     * once. This flag should be {@code true} for only <strong>one</strong> node
     * ("Leader" node) during the initial deployment.
     */
    private boolean initializeCluster = true;
    
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public IgniteProperties() {
        this.crypto.setEnabled(false);
    }
}
