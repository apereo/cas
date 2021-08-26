
package org.apereo.cas;

import org.apereo.cas.audit.DelegatedAuthenticationAuditResourceResolverTests;
import org.apereo.cas.authentication.principal.provision.ChainingDelegatedClientUserProfileProvisionerTests;
import org.apereo.cas.authentication.principal.provision.GroovyDelegatedClientUserProfileProvisionerTests;
import org.apereo.cas.authentication.principal.provision.RestfulDelegatedClientUserProfileProvisionerTests;
import org.apereo.cas.pac4j.clients.DefaultDelegatedClientIdentityProviderRedirectionStrategyTests;
import org.apereo.cas.pac4j.clients.GroovyDelegatedClientIdentityProviderRedirectionStrategyTests;
import org.apereo.cas.pac4j.discovery.DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests;
import org.apereo.cas.validation.DelegatedAuthenticationServiceTicketValidationAuthorizerTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactoryTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationSingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationRequestCustomizerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DefaultDelegatedClientIdentityProviderRedirectionStrategyTests.class,
    DelegatedClientAuthenticationRequestCustomizerTests.class,
    GroovyDelegatedClientIdentityProviderRedirectionStrategyTests.class,
    DelegatedAuthenticationAuditResourceResolverTests.class,
    DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests.class,
    DelegatedClientIdentityProviderConfigurationFactoryTests.class,
    DelegatedAuthenticationServiceTicketValidationAuthorizerTests.class,
    GroovyDelegatedClientUserProfileProvisionerTests.class,
    DelegatedAuthenticationSingleSignOnParticipationStrategyTests.class,
    ChainingDelegatedClientUserProfileProvisionerTests.class,
    RestfulDelegatedClientUserProfileProvisionerTests.class
})
@Suite
public class AllTestsSuite {
}
