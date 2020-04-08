
package org.apereo.cas;

import org.apereo.cas.audit.DelegatedAuthenticationAuditResourceResolverTests;
import org.apereo.cas.authentication.principal.provision.GroovyDelegatedClientUserProfileProvisionerTests;
import org.apereo.cas.authentication.principal.provision.RestfulDelegatedClientUserProfileProvisionerTests;
import org.apereo.cas.validation.DelegatedAuthenticationServiceTicketValidationAuthorizerTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactoryTests;

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
    DelegatedAuthenticationAuditResourceResolverTests.class,
    DelegatedClientIdentityProviderConfigurationFactoryTests.class,
    DelegatedAuthenticationServiceTicketValidationAuthorizerTests.class,
    GroovyDelegatedClientUserProfileProvisionerTests.class,
    RestfulDelegatedClientUserProfileProvisionerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
