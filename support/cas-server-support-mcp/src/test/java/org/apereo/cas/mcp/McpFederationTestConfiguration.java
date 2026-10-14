package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.CachedMetadataResolverResult;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import static org.mockito.Mockito.*;

/**
 * This is {@link McpFederationTestConfiguration}. The SAML2 metadata resolver is a stub: services whose metadata
 * location contains {@code broken} fail to resolve, those containing {@code expired} resolve expired metadata, and
 * every other entity resolves to a service provider with one POST assertion consumer service and a signing certificate.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@TestConfiguration(value = "McpFederationTestConfiguration", proxyBeanMethods = false)
public class McpFederationTestConfiguration {
    public static final String OIDC_ISSUER = "https://sso.example.org/cas/oidc";

    @Bean(name = SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() throws Exception {
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(), any())).thenAnswer(invocation -> {
            val service = invocation.getArgument(0, SamlRegisteredService.class);
            val criteria = invocation.getArgument(1, CriteriaSet.class);
            if (service.getMetadataLocation().contains("broken")) {
                throw new IOException("Unable to fetch metadata from " + service.getMetadataLocation());
            }
            val entityId = Objects.requireNonNull(criteria.get(EntityIdCriterion.class)).getEntityId();
            val metadataResolver = mock(MetadataResolver.class);
            when(metadataResolver.getId()).thenReturn("MockMetadataResolver");
            return CachedMetadataResolverResult.builder()
                .metadataResolver(metadataResolver)
                .entityDescriptor(buildEntityDescriptor(entityId, service.getMetadataLocation().contains("expired")))
                .build();
        });
        return resolver;
    }

    @Bean(name = "oidcJsonWebKeystoreGeneratorService")
    public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
        val keystore = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(new OidcProperties());
        return new OidcJsonWebKeystoreGeneratorService() {
            @Override
            public Resource generate() {
                return OidcJsonWebKeystoreGeneratorService.toResource(keystore);
            }

            @Override
            public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) {
                return jsonWebKeySet;
            }

            @Override
            public Optional<Resource> find() {
                return Optional.of(generate());
            }
        };
    }

    @Bean(name = OidcIssuerService.BEAN_NAME)
    public OidcIssuerService oidcIssuerService() {
        return OidcIssuerService.echoing(OIDC_ISSUER);
    }

    private static EntityDescriptor buildEntityDescriptor(final String entityId, final boolean expired) throws Exception {
        val acs = mock(AssertionConsumerService.class);
        when(acs.getBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(acs.getLocation()).thenReturn(entityId + "/acs");
        when(acs.getIndex()).thenReturn(0);
        when(acs.isDefault()).thenReturn(Boolean.TRUE);

        val slo = mock(SingleLogoutService.class);
        when(slo.getBinding()).thenReturn(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        when(slo.getLocation()).thenReturn(entityId + "/slo");

        val nameIdFormat = mock(NameIDFormat.class);
        when(nameIdFormat.getURI()).thenReturn("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");

        val certificate = mock(org.opensaml.xmlsec.signature.X509Certificate.class);
        try (val in = new ClassPathResource("sp-signing-certificate.b64").getInputStream()) {
            when(certificate.getValue()).thenReturn(IOUtils.toString(in, StandardCharsets.UTF_8));
        }
        val x509Data = mock(X509Data.class);
        when(x509Data.getX509Certificates()).thenReturn(List.of(certificate));
        val keyInfo = mock(KeyInfo.class);
        when(keyInfo.getX509Datas()).thenReturn(List.of(x509Data));
        val keyDescriptor = mock(KeyDescriptor.class);
        when(keyDescriptor.getUse()).thenReturn(UsageType.SIGNING);
        when(keyDescriptor.getKeyInfo()).thenReturn(keyInfo);

        val descriptor = mock(SPSSODescriptor.class);
        when(descriptor.getSupportedProtocols()).thenReturn(List.of(SAMLConstants.SAML20P_NS));
        when(descriptor.isAuthnRequestsSigned()).thenReturn(Boolean.FALSE);
        when(descriptor.getWantAssertionsSigned()).thenReturn(Boolean.TRUE);
        when(descriptor.getNameIDFormats()).thenReturn(List.of(nameIdFormat));
        when(descriptor.getAssertionConsumerServices()).thenReturn(List.of(acs));
        when(descriptor.getSingleLogoutServices()).thenReturn(List.of(slo));
        when(descriptor.getKeyDescriptors()).thenReturn(List.of(keyDescriptor));

        val entityDescriptor = mock(EntityDescriptor.class);
        when(entityDescriptor.getEntityID()).thenReturn(entityId);
        when(entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS)).thenReturn(descriptor);
        val now = Instant.now(Clock.systemUTC());
        when(entityDescriptor.getValidUntil()).thenReturn(expired ? now.minus(Duration.ofDays(1)) : now.plus(Duration.ofDays(30)));
        return entityDescriptor;
    }
}
