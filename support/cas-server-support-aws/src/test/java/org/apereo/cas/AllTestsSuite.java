
package org.apereo.cas;

import org.apereo.cas.aws.AmazonClientConfigurationBuilderTests;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilderTests;
import org.apereo.cas.aws.AmazonSecurityTokenServiceEndpointTests;
import org.apereo.cas.aws.ChainingAWSCredentialsProviderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    AmazonSecurityTokenServiceEndpointTests.class,
    AmazonEnvironmentAwareClientBuilderTests.class,
    AmazonClientConfigurationBuilderTests.class,
    ChainingAWSCredentialsProviderTests.class
})
@Suite
public class AllTestsSuite {
}
