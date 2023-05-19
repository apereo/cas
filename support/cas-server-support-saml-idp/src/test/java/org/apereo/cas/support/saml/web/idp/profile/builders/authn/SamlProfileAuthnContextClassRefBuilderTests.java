package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlProfileAuthnContextClassRefBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLResponse")
public class SamlProfileAuthnContextClassRefBuilderTests {
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BaseSamlIdPConfigurationTests {
        @Test
        public void verifyGroovyOperationByService() throws Exception {
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

            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals("https://refeds.org/profile/mfa", result.getAuthnContextClassRef().getURI());
        }

        @Test
        public void verifyOperationByService() throws Exception {
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

            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals(service.getRequiredAuthenticationContextClass(), result.getAuthnContextClassRef().getURI());
        }

        @Test
        public void verifyOperationByAuthnRequest() throws Exception {
            val service = getSamlRegisteredServiceForTestShib();
            val authnRequest = getAuthnRequestFor(service);
            val context = mock(RequestedAuthnContext.class);
            when(context.getAuthnContextClassRefs()).thenReturn(List.of());
            authnRequest.setRequestedAuthnContext(context);
            val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
                samlRegisteredServiceCachingMetadataResolver, service, authnRequest);

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(authnRequest)
                .authenticatedAssertion(Optional.of(getAssertion()))
                .registeredService(service)
                .adaptor(adaptor.get())
                .build();
            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals(AuthnContext.PPT_AUTHN_CTX, result.getAuthnContextClassRef().getURI());
        }

        @Test
        public void verifyOperationByAssertion() throws Exception {
            val service = getSamlRegisteredServiceForTestShib();
            val authnRequest = getAuthnRequestFor(service);

            val classRef = mock(AuthnContextClassRef.class);
            when(classRef.getURI()).thenReturn("some-context");
            val context = mock(RequestedAuthnContext.class);
            when(context.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
            authnRequest.setRequestedAuthnContext(context);
            val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
                samlRegisteredServiceCachingMetadataResolver, service, authnRequest);

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(authnRequest)
                .authenticatedAssertion(Optional.of(getAssertion()))
                .registeredService(service)
                .adaptor(adaptor.get())
                .build();
            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals(AuthnContext.PPT_AUTHN_CTX, result.getAuthnContextClassRef().getURI());
        }
    }
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.saml-idp.core.authentication-context-class-mappings[0]=https://refeds.org/profile/mfa->" + TestMultifactorAuthenticationProvider.ID)
    public class MappedToMfaProviderTests extends BaseSamlIdPConfigurationTests {

        @Test
        public void verifyRefedsContext() throws Exception {
            val service = getSamlRegisteredServiceForTestShib();
            val authnRequest = getAuthnRequestFor(service);

            val classRef = mock(AuthnContextClassRef.class);
            when(classRef.getURI()).thenReturn("https://refeds.org/profile/mfa");
            val context = mock(RequestedAuthnContext.class);
            when(context.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
            authnRequest.setRequestedAuthnContext(context);
            val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
                samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
            val assertion = getAssertion(Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                TestMultifactorAuthenticationProvider.ID));

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(authnRequest)
                .authenticatedAssertion(Optional.of(assertion))
                .registeredService(service)
                .adaptor(adaptor.get())
                .build();
            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals("https://refeds.org/profile/mfa", result.getAuthnContextClassRef().getURI());
        }
    }
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.mfa.core.authentication-context-attribute=amr",
        "cas.authn.saml-idp.core.authentication-context-class-mappings[0]=https://refeds.org/profile/mfa->mfa"
    })
    public class MappedToValueTests extends BaseSamlIdPConfigurationTests {

        @Test
        public void verifyRefedsContextWithPrincipalAttribute() throws Exception {
            val service = getSamlRegisteredServiceForTestShib();
            val authnRequest = getAuthnRequestFor(service);

            val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
                samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
            val assertion = getAssertion(
                Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), List.of("pwd", "mfa")));

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(authnRequest)
                .authenticatedAssertion(Optional.of(assertion))
                .registeredService(service)
                .adaptor(adaptor.get())
                .build();
            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals("https://refeds.org/profile/mfa", result.getAuthnContextClassRef().getURI());
        }

        @Test
        public void verifyRefedsContextWithoutPrincipalAttribute() throws Exception {
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
            val result = defaultAuthnContextClassRefBuilder.build(buildContext);
            assertEquals(AuthnContext.PPT_AUTHN_CTX, result.getAuthnContextClassRef().getURI());
        }
    }

}
