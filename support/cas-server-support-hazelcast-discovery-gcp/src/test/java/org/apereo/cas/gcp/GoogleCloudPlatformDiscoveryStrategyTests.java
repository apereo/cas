package org.apereo.cas.gcp;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudPlatformDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Hazelcast")
public class GoogleCloudPlatformDiscoveryStrategyTests {
    @Test
    public void verifyOperation() throws Exception {
        val cluster = new HazelcastClusterProperties();
        val gcp = cluster.getDiscovery().getGcp();
        gcp.setLabel("label");
        gcp.setRegion("region");
        gcp.setZones("zone1");
        gcp.setProjects("project1");
        gcp.setPrivateKeyPath(File.createTempFile("sample", ".json").getAbsolutePath());
        val hz = new GoogleCloudPlatformDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
    }
}
