
package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfigurationCachingAttributeRepositoryTests;
import org.apereo.cas.config.CasPersonDirectoryConfigurationCascadeAggregationTests;
import org.apereo.cas.config.CasPersonDirectoryConfigurationMergeAggregationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    PersonDirectoryAttributeRepositoryPlanTests.class,
    DefaultPersonDirectoryAttributeRepositoryPlanLdapTests.class,
    PersonDirectoryPrincipalResolverConcurrencyTests.class,
    PrincipalAttributeRepositoryFetcherCascadeTests.class,
    CachingAttributeRepositoryTests.class,
    PrincipalAttributeRepositoryFetcherJdbcTests.class,
    PersonDirectoryPrincipalResolverLdapTests.class,
    PersonDirectoryPrincipalResolverOpenLdapTests.class,
    PersonDirectoryPrincipalResolverActiveDirectoryTests.class,
    PrincipalAttributeRepositoryFetcherLdapTests.class,
    PrincipalAttributeRepositoryFetcherTests.class,
    CasPersonDirectoryConfigurationCachingAttributeRepositoryTests.class,
    CasPersonDirectoryConfigurationMergeAggregationTests.class,
    CasPersonDirectoryConfigurationCascadeAggregationTests.class,
    DefaultAttributeDefinitionStoreTests.class,
    JdbcSingleRowAttributeRepositoryPostgresTests.class
})
@Suite
public class AllTestsSuite {
}
