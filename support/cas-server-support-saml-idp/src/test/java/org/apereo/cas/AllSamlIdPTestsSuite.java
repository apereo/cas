package org.apereo.cas;

import org.apereo.cas.support.saml.SamlAttributeEncoderTests;
import org.apereo.cas.support.saml.SamlIdPConfigurationTests;
import org.apereo.cas.support.saml.authentication.SamlIdPServiceFactoryTests;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocatorTests;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataResolverTests;
import org.apereo.cas.support.saml.services.EduPersonTargetedIdAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.GroovySamlRegisteredServiceAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.MetadataRequestedAttributesAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.RefedsRSAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceAttributeReleasePolicyTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceJpaMicrosoftSqlServerTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceJpaPostgresTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceJpaTests;
import org.apereo.cas.support.saml.services.SamlRegisteredServiceTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacadeTests;
import org.apereo.cas.support.saml.services.logout.SamlIdPProfileSingleLogoutMessageCreatorTests;
import org.apereo.cas.support.saml.services.logout.SamlIdPSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.support.saml.util.SamlIdPUtilsTests;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdPMetadataControllerTests;
import org.apereo.cas.support.saml.web.idp.metadata.SamlRegisteredServiceCachedMetadataEndpointTests;
import org.apereo.cas.support.saml.web.idp.profile.SamlIdPInitiatedProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.artifact.CasSamlArtifactMapTests;
import org.apereo.cas.support.saml.web.idp.profile.artifact.SamlIdPSaml1ArtifactResolutionProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlProfileSamlRegisteredServiceAttributeBuilderTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidatorTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.nameid.SamlProfileSamlNameIdBuilderTests;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilderTests;
import org.apereo.cas.support.saml.web.idp.profile.ecp.ECPSamlIdPProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.query.SamlIdPSaml2AttributeQueryProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOSamlIdPPostProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOSamlRedirectProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPHttpRedirectDeflateEncoderTests;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceMessageHandlerTests;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostProfileHandlerEndpointTests;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostSimpleSignProfileHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPProfileCallbackHandlerControllerTests;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.DefaultSSOSamlHttpRequestExtractorTests;
import org.apereo.cas.support.saml.web.velocity.SamlTemplatesVelocityEngineTests;
import org.apereo.cas.ticket.query.DefaultSamlAttributeQueryTicketFactoryTests;
import org.apereo.cas.web.flow.SamlIdPConsentableAttributeBuilderTests;
import org.apereo.cas.web.flow.SamlIdPMetadataUIWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    SamlRegisteredServiceTests.class,
    SamlIdPConfigurationTests.class,
    SamlAttributeEncoderTests.class,
    SamlRegisteredServiceJpaTests.class,
    SamlProfileSamlNameIdBuilderTests.class,
    SamlIdPProfileSingleLogoutMessageCreatorTests.class,
    SamlIdPSingleLogoutServiceLogoutUrlBuilderTests.class,
    SamlRegisteredServiceJpaMicrosoftSqlServerTests.class,
    PatternMatchingEntityIdAttributeReleasePolicyTests.class,
    SamlProfileSamlRegisteredServiceAttributeBuilderTests.class,
    GroovySamlRegisteredServiceAttributeReleasePolicyTests.class,
    SamlRegisteredServiceJpaPostgresTests.class,
    SamlIdPUtilsTests.class,
    SamlIdPSaml1ArtifactResolutionProfileHandlerControllerTests.class,
    SamlIdPSingleLogoutServiceMessageHandlerTests.class,
    CasSamlArtifactMapTests.class,
    SamlIdPHttpRedirectDeflateEncoderTests.class,
    SamlIdPInitiatedProfileHandlerControllerTests.class,
    SamlIdPMetadataControllerTests.class,
    SamlRegisteredServiceServiceProviderMetadataFacadeTests.class,
    SamlRegisteredServiceAttributeReleasePolicyTests.class,
    SamlIdPConsentableAttributeBuilderTests.class,
    SamlIdPServiceFactoryTests.class,
    SamlIdPMetadataResolverTests.class,
    SSOSamlIdPProfileCallbackHandlerControllerTests.class,
    SLOSamlIdPPostProfileHandlerControllerTests.class,
    SamlRegisteredServiceCachedMetadataEndpointTests.class,
    SamlIdPMetadataUIWebflowConfigurerTests.class,
    FileSystemSamlIdPMetadataLocatorTests.class,
    FileSystemSamlIdPMetadataGeneratorTests.class,
    DefaultSamlAttributeQueryTicketFactoryTests.class,
    DefaultSSOSamlHttpRequestExtractorTests.class,
    InCommonRSAttributeReleasePolicyTests.class,
    SSOSamlIdPPostSimpleSignProfileHandlerControllerTests.class,
    RefedsRSAttributeReleasePolicyTests.class,
    SSOSamlIdPPostProfileHandlerEndpointTests.class,
    MetadataRequestedAttributesAttributeReleasePolicyTests.class,
    SamlObjectSignatureValidatorTests.class,
    SamlTemplatesVelocityEngineTests.class,
    ECPSamlIdPProfileHandlerControllerTests.class,
    SamlIdPSaml2AttributeQueryProfileHandlerControllerTests.class,
    SSOSamlIdPPostProfileHandlerControllerTests.class,
    SLOSamlRedirectProfileHandlerControllerTests.class,
    EduPersonTargetedIdAttributeReleasePolicyTests.class,
    SamlProfileSaml2ResponseBuilderTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSamlIdPTestsSuite {
}

