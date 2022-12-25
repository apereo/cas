package org.apereo.cas;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfileTests;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessorTests;
import org.apereo.cas.integration.pac4j.BrowserWebStorageSessionStoreTests;
import org.apereo.cas.integration.pac4j.DistributedJEESessionStoreTests;
import org.apereo.cas.pac4j.serialization.NimbusOAuthJacksonModuleTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DelegatedAuthenticationCandidateProfileTests.class,
    DelegatedAuthenticationPreProcessorTests.class,
    BrowserWebStorageSessionStoreTests.class,
    DistributedJEESessionStoreTests.class,
    NimbusOAuthJacksonModuleTests.class
})
@Suite
public class AllTestsSuite {
}
