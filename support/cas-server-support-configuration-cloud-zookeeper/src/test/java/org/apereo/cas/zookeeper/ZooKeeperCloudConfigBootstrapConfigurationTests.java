package org.apereo.cas.zookeeper;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigAutoConfiguration;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigBootstrapConfiguration;
import org.springframework.retry.annotation.EnableRetry;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ZooKeeperCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    ZookeeperConfigBootstrapConfiguration.class,
    ZookeeperConfigAutoConfiguration.class
}, properties = {
    "spring.cloud.zookeeper.enabled=true",
    "spring.cloud.zookeeper.config.watcher.enabled=true",
    "spring.cloud.zookeeper.config.enabled=true",
    "spring.cloud.zookeeper.connect-string=localhost:2181",
    "spring.cloud.zookeeper.enabled=true",
    "spring.application.name=cas"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("ZooKeeper")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 2181)
@EnableRetry
class ZooKeeperCloudConfigBootstrapConfigurationTests {
    @Autowired
    @Qualifier("curatorFramework")
    private CuratorFramework curatorFramework;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void setup() throws Exception {
        FunctionUtils.doAndRetry(List.of(KeeperException.ConnectionLossException.class), context -> {
            val curator = CuratorFrameworkFactory.newClient("localhost:2181",
                5000, 5000, new RetryNTimes(2, 100));
            curator.start();
            val path = "/config/cas/cas/server/name";

            val zk = curator.getZookeeperClient().getZooKeeper();
            if (zk.exists(path, false) != null) {
                curator.delete().forPath(path);
            }
            curator
                .create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(path, "apereocas".getBytes(StandardCharsets.UTF_8));
            curator.close();
            return null;
        });
    }

    @Test
    void verifyOperation() throws Throwable {
        val zk = curatorFramework.getZookeeperClient().getZooKeeper();
        assertNotNull(zk);
        assertEquals("apereocas", casProperties.getServer().getName());
    }
}
