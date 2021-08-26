package org.apereo.cas.ws.idp;

import org.apereo.cas.ws.idp.services.CustomNamespaceWSFederationClaimsReleasePolicyTests;
import org.apereo.cas.ws.idp.services.WSFederationClaimsReleasePolicyTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link org.apereo.cas.AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    CustomNamespaceWSFederationClaimsReleasePolicyTests.class,
    WSFederationClaimsReleasePolicyTests.class
})
@Suite
public class AllTestsSuite {
}

