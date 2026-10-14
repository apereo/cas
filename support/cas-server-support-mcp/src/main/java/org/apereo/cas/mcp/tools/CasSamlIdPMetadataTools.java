package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
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
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> resolveSamlMetadata(
        @McpToolParam(description = "The entity id of the SAML2 service provider")
        final String entityId) {
        val result = new LinkedHashMap<String, Object>();
        result.put("entityId", entityId);
        val resolver = metadataResolver.getIfAvailable();
        if (resolver == null) {
            result.put("resolved", false);
            result.put("reason", "The SAML2 identity provider is not configured");
            return result;
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
            result.put("resolved", false);
            result.put("reason", "No SAML2 service provider registration matches " + entityId);
            return result;
        }
        val registration = new LinkedHashMap<String, Object>();
        registration.put("id", registeredService.getId());
        registration.put("name", registeredService.getName());
        registration.put("serviceId", registeredService.getServiceId());
        registration.put("metadataLocation", registeredService.getMetadataLocation());
        result.put("registeredService", registration);

        val criteria = new CriteriaSet(new EntityIdCriterion(entityId),
            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        try {
            val cachedResult = resolver.resolve(registeredService, criteria);
            if (cachedResult == null || !cachedResult.isResolved()) {
                result.put("resolved", false);
                result.put("reason", "Metadata could not be resolved from " + registeredService.getMetadataLocation());
                return result;
            }
            val chainingResolver = cachedResult.getMetadataResolver();
            result.put("metadataResolver", chainingResolver.getId());
            val entityDescriptor = cachedResult.getEntityDescriptor().isPresent()
                ? cachedResult.getEntityDescriptor().get()
                : chainingResolver.resolveSingle(criteria);
            if (entityDescriptor == null) {
                result.put("resolved", false);
                result.put("reason", "The metadata does not contain the entity " + entityId);
                return result;
            }
            describe(entityDescriptor, result);
        } catch (final Throwable e) {
            result.put("resolved", false);
            result.put("error", Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
        }
        return result;
    }

    private static void describe(final EntityDescriptor entityDescriptor, final Map<String, Object> result) {
        val now = Instant.now(Clock.systemUTC());
        putValidity(result, entityDescriptor.getValidUntil(), now);
        val descriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (descriptor == null) {
            result.put("resolved", false);
            result.put("reason", "The metadata has no SAML2 service provider descriptor for this entity");
            return;
        }
        val expired = Boolean.TRUE.equals(result.get("expired"));
        val sp = new LinkedHashMap<String, Object>();
        putValidity(sp, descriptor.getValidUntil(), now);
        sp.put("supportedProtocols", descriptor.getSupportedProtocols());
        sp.put("authnRequestsSigned", Boolean.TRUE.equals(descriptor.isAuthnRequestsSigned()));
        sp.put("wantAssertionsSigned", Boolean.TRUE.equals(descriptor.getWantAssertionsSigned()));
        sp.put("nameIdFormats", descriptor.getNameIDFormats().stream().map(NameIDFormat::getURI).filter(Objects::nonNull).toList());
        sp.put("assertionConsumerServices", descriptor.getAssertionConsumerServices()
            .stream()
            .map(acs -> {
                val endpoint = new LinkedHashMap<String, Object>();
                endpoint.put("binding", acs.getBinding());
                endpoint.put("location", acs.getLocation());
                endpoint.put("index", acs.getIndex());
                endpoint.put("default", Boolean.TRUE.equals(acs.isDefault()));
                return endpoint;
            })
            .toList());
        sp.put("singleLogoutServices", descriptor.getSingleLogoutServices()
            .stream()
            .map(slo -> {
                val endpoint = new LinkedHashMap<String, Object>();
                endpoint.put("binding", slo.getBinding());
                endpoint.put("location", slo.getLocation());
                return endpoint;
            })
            .toList());
        sp.put("keys", descriptor.getKeyDescriptors().stream().map(key -> describeKey(key, now)).toList());
        result.put("serviceProvider", sp);
        result.put("resolved", !expired && !Boolean.TRUE.equals(sp.get("expired")));
        if (descriptor.getAssertionConsumerServices().isEmpty()) {
            result.put("warning", "The service provider descriptor has no assertion consumer service");
        }
    }

    private static Map<String, Object> describeKey(final KeyDescriptor keyDescriptor, final Instant now) {
        val key = new LinkedHashMap<String, Object>();
        key.put("use", keyDescriptor.getUse() != null ? keyDescriptor.getUse().name() : "UNSPECIFIED");
        val keyInfo = keyDescriptor.getKeyInfo();
        if (keyInfo != null) {
            try {
                key.put("certificates", KeyInfoSupport.getCertificates(keyInfo)
                    .stream()
                    .map(certificate -> {
                        val details = new LinkedHashMap<String, Object>();
                        details.put("subject", certificate.getSubjectX500Principal().getName());
                        details.put("issuer", certificate.getIssuerX500Principal().getName());
                        details.put("serialNumber", HexFormat.of().formatHex(certificate.getSerialNumber().toByteArray()));
                        details.put("notBefore", certificate.getNotBefore().toInstant().toString());
                        details.put("notAfter", certificate.getNotAfter().toInstant().toString());
                        details.put("expired", certificate.getNotAfter().toInstant().isBefore(now));
                        return details;
                    })
                    .toList());
            } catch (final Throwable e) {
                key.put("error", Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
            }
        }
        return key;
    }

    private static void putValidity(final Map<String, Object> target, final @Nullable Instant validUntil, final Instant now) {
        if (validUntil != null) {
            target.put("validUntil", validUntil.toString());
            target.put("expired", validUntil.isBefore(now));
        }
    }
}
