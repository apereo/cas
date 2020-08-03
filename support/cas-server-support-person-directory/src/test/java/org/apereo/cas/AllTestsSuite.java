
package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfigurationCascadeAggregationTests;
import org.apereo.cas.config.CasPersonDirectoryConfigurationMergeAggregationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    JdbcMultiRowAttributeRepositoryTests.class,
    JdbcSingleRowAttributeRepositoryTests.class,
    RestfulPersonAttributeDaoTests.class,
    DefaultPersonDirectoryAttributeRepositoryPlanLdapTests.class,
    PersonDirectoryPrincipalResolverConcurrencyTests.class,
    PrincipalAttributeRepositoryFetcherCascadeTests.class,
    CachingAttributeRepositoryTests.class,
    CasPersonDirectoryConfigurationMergeAggregationTests.class,
    CasPersonDirectoryConfigurationCascadeAggregationTests.class,
    DefaultAttributeDefinitionStoreTests.class,
    JdbcSingleRowAttributeRepositoryPostgresTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
