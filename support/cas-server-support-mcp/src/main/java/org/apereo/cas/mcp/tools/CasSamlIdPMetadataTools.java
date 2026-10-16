package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasSamlIdPMetadataTools}, available when the SAML2 identity provider is included in the build.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSamlIdPMetadataTools {
    private final ServicesManager servicesManager;

    private final ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> metadataResolver;

    /**
     * Resolve and describe the metadata of a SAML2 service provider.
     *
     * @param entityId the service provider entity id
     * @return the metadata summary, or why it cannot be resolved
     */
    @McpTool(description = "Resolve the metadata of a SAML2 service provider the way the CAS SAML2 identity provider does, "
        + "and report whether it resolves, the matching registration and metadata source, the validity period, the "
        + "assertion consumer and single logout endpoints, the name id formats and the signing and encryption certificates "
        + "with their expiration dates. When resolution fails, the error is returned.",
        title = "Resolve SAML2 Metadata",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public SamlMetadata resolveSamlMetadata(
        @McpToolParam(description = "The entity id of the SAML2 service provider")
        final String entityId) {
        val result = SamlMetadata.builder().entityId(entityId).resolved(false);
        val resolver = metadataResolver.getIfAvailable();
        if (resolver == null) {
            return result.reason("The SAML2 identity provider is not configured").build();
        }
        val registeredService = servicesManager.getAllServicesOfType(SamlRegisteredService.class)
            .stream()
            .filter(Objects::nonNull)
            .map(SamlRegisteredService.class::cast)
            .sorted()
            .filter(service -> service.matches(entityId))
            .findFirst()
            .orElse(null);
        if (registeredService == null) {
            return result.reason("No SAML2 service provider registration matches " + entityId).build();
        }
        result.registeredService(new SamlRegistration(registeredService.getId(), registeredService.getName(),
            registeredService.getServiceId(), registeredService.getMetadataLocation()));

        val criteria = new CriteriaSet(new EntityIdCriterion(entityId),
            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        try {
            val cachedResult = resolver.resolve(registeredService, criteria);
            if (cachedResult == null || !cachedResult.isResolved()) {
                return result.reason("Metadata could not be resolved from " + registeredService.getMetadataLocation()).build();
            }
            val chainingResolver = cachedResult.getMetadataResolver();
            result.metadataResolver(chainingResolver.getId());
            val entityDescriptor = cachedResult.getEntityDescriptor().isPresent()
                ? cachedResult.getEntityDescriptor().get()
                : chainingResolver.resolveSingle(criteria);
            if (entityDescriptor == null) {
                return result.reason("The metadata does not contain the entity " + entityId).build();
            }
            return describe(entityDescriptor, result);
        } catch (final Throwable e) {
            return result.resolved(false).error(Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName())).build();
        }
    }

    private static SamlMetadata describe(final EntityDescriptor entityDescriptor, final SamlMetadata.SamlMetadataBuilder result) {
        val now = Instant.now(Clock.systemUTC());
        val validUntil = entityDescriptor.getValidUntil();
        val expired = validUntil != null && validUntil.isBefore(now);
        result.validUntil(validUntil != null ? validUntil.toString() : null).expired(validUntil != null ? expired : null);
        val descriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (descriptor == null) {
            return result.resolved(false).reason("The metadata has no SAML2 service provider descriptor for this entity").build();
        }
        val spValidUntil = descriptor.getValidUntil();
        val spExpired = spValidUntil != null && spValidUntil.isBefore(now);
        val serviceProvider = new ServiceProvider(
            spValidUntil != null ? spValidUntil.toString() : null,
            spValidUntil != null ? spExpired : null,
            List.copyOf(descriptor.getSupportedProtocols()),
            Boolean.TRUE.equals(descriptor.isAuthnRequestsSigned()),
            Boolean.TRUE.equals(descriptor.getWantAssertionsSigned()),
            descriptor.getNameIDFormats().stream().map(NameIDFormat::getURI).filter(Objects::nonNull).toList(),
            descriptor.getAssertionConsumerServices()
                .stream()
                .map(acs -> new Endpoint(acs.getBinding(), acs.getLocation(), acs.getIndex(), Boolean.TRUE.equals(acs.isDefault())))
                .toList(),
            descriptor.getSingleLogoutServices()
                .stream()
                .map(slo -> new Endpoint(slo.getBinding(), slo.getLocation(), null, null))
                .toList(),
            descriptor.getKeyDescriptors().stream().map(key -> describeKey(key, now)).toList());
        result.serviceProvider(serviceProvider).resolved(!expired && !spExpired);
        if (descriptor.getAssertionConsumerServices().isEmpty()) {
            result.warning("The service provider descriptor has no assertion consumer service");
        }
        return result.build();
    }

    private static KeySummary describeKey(final KeyDescriptor keyDescriptor, final Instant now) {
        val use = keyDescriptor.getUse() != null ? keyDescriptor.getUse().name() : "UNSPECIFIED";
        val keyInfo = keyDescriptor.getKeyInfo();
        if (keyInfo == null) {
            return new KeySummary(use, null, null);
        }
        try {
            val certificates = KeyInfoSupport.getCertificates(keyInfo)
                .stream()
                .map(certificate -> new CertificateSummary(certificate.getSubjectX500Principal().getName(),
                    certificate.getIssuerX500Principal().getName(),
                    HexFormat.of().formatHex(certificate.getSerialNumber().toByteArray()),
                    certificate.getNotBefore().toInstant().toString(),
                    certificate.getNotAfter().toInstant().toString(),
                    certificate.getNotAfter().toInstant().isBefore(now)))
                .toList();
            return new KeySummary(use, certificates, null);
        } catch (final Throwable e) {
            return new KeySummary(use, null, Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
        }
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SamlMetadata(String entityId, boolean resolved, @Nullable SamlRegistration registeredService,
                               @Nullable String metadataResolver, @Nullable String validUntil, @Nullable Boolean expired,
                               @Nullable ServiceProvider serviceProvider, @Nullable String warning, @Nullable String reason,
                               @Nullable String error) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SamlRegistration(long id, @Nullable String name, @Nullable String serviceId, @Nullable String metadataLocation) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ServiceProvider(@Nullable String validUntil, @Nullable Boolean expired, List<String> supportedProtocols,
                                  boolean authnRequestsSigned, boolean wantAssertionsSigned, List<String> nameIdFormats,
                                  List<Endpoint> assertionConsumerServices, List<Endpoint> singleLogoutServices,
                                  List<KeySummary> keys) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Endpoint(@Nullable String binding, @Nullable String location, @Nullable Integer index,
                           @JsonProperty("default") @Nullable Boolean defaultEndpoint) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record KeySummary(String use, @Nullable List<CertificateSummary> certificates, @Nullable String error) {
    }

    public record CertificateSummary(String subject, String issuer, String serialNumber, String notBefore, String notAfter,
                                     boolean expired) {
    }
}
