package org.apereo.cas;

import org.apereo.cas.zookeeper.HazelcastZooKeeperDiscoveryStrategyTests;
import org.apereo.cas.zookeeper.ZookeeperLockRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SelectClasses({
    HazelcastZooKeeperDiscoveryStrategyTests.class,
    ZookeeperLockRegistryTests.class
})
@Suite
public class AllTestsSuite {
}
