package org.apereo.cas.zookeeper;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigAutoConfiguration;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigBootstrapConfiguration;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ZooKeeperCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    ZookeeperConfigBootstrapConfiguration.class,
    ZookeeperConfigAutoConfiguration.class,
}, properties = {
    "spring.cloud.zookeeper.enabled=true",
    "spring.cloud.zookeeper.config.watcher.enabled=true",
    "spring.cloud.zookeeper.config.enabled=true",
    "spring.cloud.zookeeper.connectString=localhost:2181",
    "spring.cloud.zookeeper.enabled=true",
    "spring.application.name=cas"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfContinuousIntegration
@Tag("ZooKeeper")
@EnabledIfPortOpen(port = 2181)
public class ZooKeeperCloudConfigBootstrapConfigurationTests {
    @Autowired
    @Qualifier("curatorFramework")
    private CuratorFramework curatorFramework;

    @Test
    public void verifyOperation() throws Exception {
        val zk = curatorFramework.getZookeeperClient().getZooKeeper();
        assertNotNull(zk);
        zk.create("/cas/config/cas/server/name", "ApereoCAS".getBytes(StandardCharsets.UTF_8),
            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
}
