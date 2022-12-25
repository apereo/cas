package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSamlProfileAuthnContextClassRefBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2")
public class DefaultSamlProfileAuthnContextClassRefBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyGroovyOperationByService() throws Exception {
        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredAuthenticationContextClass("classpath:SamlAuthnContext.groovy");
        val authnRequest = getAuthnRequestFor(service);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();

        val result = builder.build(buildContext);
        assertEquals("https://refeds.org/profile/mfa", result);
    }

    @Test
    public void verifyOperationByService() throws Exception {
        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredAuthenticationContextClass("some-context-class");
        val authnRequest = getAuthnRequestFor(service);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();

        val result = builder.build(buildContext);
        assertEquals(service.getRequiredAuthenticationContextClass(), result);
    }

    @Test
    public void verifyOperationByAuthnRequest() throws Exception {
        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        val context = mock(RequestedAuthnContext.class);
        when(context.getAuthnContextClassRefs()).thenReturn(List.of());
        when(authnRequest.getRequestedAuthnContext()).thenReturn(context);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();
        val result = builder.build(buildContext);
        assertEquals(AuthnContext.PPT_AUTHN_CTX, result);
    }

    @Test
    public void verifyOperationByAssertion() throws Exception {
        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);

        val classRef = mock(AuthnContextClassRef.class);
        when(classRef.getURI()).thenReturn("some-context");
        val context = mock(RequestedAuthnContext.class);
        when(context.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
        when(authnRequest.getRequestedAuthnContext()).thenReturn(context);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(getAssertion()))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();
        val result = builder.build(buildContext);
        assertEquals(AuthnContext.PPT_AUTHN_CTX, result);
    }

    @Test
    public void verifyRefedsContext() throws Exception {
        val props = new CasConfigurationProperties();
        props.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings()
            .add(String.format("https://refeds.org/profile/mfa->%s", TestMultifactorAuthenticationProvider.ID));

        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(props);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);

        val classRef = mock(AuthnContextClassRef.class);
        when(classRef.getURI()).thenReturn("https://refeds.org/profile/mfa");
        val context = mock(RequestedAuthnContext.class);
        when(context.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
        when(authnRequest.getRequestedAuthnContext()).thenReturn(context);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
        val assertion = getAssertion(Map.of(props.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
            TestMultifactorAuthenticationProvider.ID));

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(assertion))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();
        val result = builder.build(buildContext);
        assertEquals("https://refeds.org/profile/mfa", result);
    }

    @Test
    public void verifyRefedsContextWithPrincipalAttribute() throws Exception {
        val props = new CasConfigurationProperties();
        props.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings()
            .add("https://refeds.org/profile/mfa->mfa");
        props.getAuthn().getMfa().getCore().setAuthenticationContextAttribute("amr");

        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(props);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
        val assertion = getAssertion(
            Map.of(props.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), List.of("pwd", "mfa")));

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(assertion))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();
        val result = builder.build(buildContext);
        assertEquals("https://refeds.org/profile/mfa", result);
    }

    @Test
    public void verifyRefedsContextWithoutPrincipalAttribute() throws Exception {
        val props = new CasConfigurationProperties();
        props.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings()
            .add("https://refeds.org/profile/mfa->mfa");
        props.getAuthn().getMfa().getCore().setAuthenticationContextAttribute("amr");

        val builder = new DefaultSamlProfileAuthnContextClassRefBuilder(props);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
        val assertion = getAssertion(Map.of());
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .authenticatedAssertion(Optional.of(assertion))
            .registeredService(service)
            .adaptor(adaptor.get())
            .build();
        val result = builder.build(buildContext);
        assertEquals(AuthnContext.PPT_AUTHN_CTX, result);
    }
}
