package org.apereo.cas;

import org.apereo.cas.scim.v1.ScimV1PrincipalAttributeMapperTests;
import org.apereo.cas.scim.v2.ScimV2PrincipalAttributeMapperTests;
import org.apereo.cas.web.flow.PrincipalScimV1ProvisionerActionTests;
import org.apereo.cas.web.flow.PrincipalScimV2ProvisionerActionTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllScimTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ScimV1PrincipalAttributeMapperTests.class,
    ScimV2PrincipalAttributeMapperTests.class,
    PrincipalScimV1ProvisionerActionTests.class,
    PrincipalScimV2ProvisionerActionTests.class
})
public class AllScimTestsSuite {
}
