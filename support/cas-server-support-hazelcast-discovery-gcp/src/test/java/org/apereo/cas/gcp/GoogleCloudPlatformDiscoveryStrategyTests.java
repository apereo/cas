package org.apereo.cas.gcp;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.test.CasTestExtension;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudPlatformDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class GoogleCloudPlatformDiscoveryStrategyTests {
    @Test
    void verifyOperation() throws Throwable {
        val cluster = new HazelcastClusterProperties();
        val gcp = cluster.getDiscovery().getGcp();
        gcp.setLabel("label");
        gcp.setRegion("region");
        gcp.setZones("zone1");
        gcp.setProjects("project1");
        gcp.setPrivateKeyPath(Files.createTempFile("sample", ".json").toFile().getAbsolutePath());
        val hz = new GoogleCloudPlatformDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
    }
}
