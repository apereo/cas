package org.apereo.cas;

import org.apereo.cas.rest.audit.RestResponseEntityAuditResourceResolverTests;
import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactoryTests;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    UsernamePasswordRestHttpRequestCredentialFactoryTests.class,
    RestResponseEntityAuditResourceResolverTests.class,
    ChainingRestHttpRequestCredentialFactoryTests.class
})
@Suite
public class AllTestsSuite {
}
