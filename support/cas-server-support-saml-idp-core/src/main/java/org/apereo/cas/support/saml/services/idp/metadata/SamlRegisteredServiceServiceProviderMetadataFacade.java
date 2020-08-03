package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.ObjectUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.xmlsec.signature.Signature;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link SamlRegisteredServiceServiceProviderMetadataFacade} that acts a façade between the SAML metadata resolved
 * from a metadata resource and other outer layers in CAS that need access to the bits of that
 * metadata. Once the metadata resolved for a service is adapted and parsed for a given entity id,
 * callers will be able to peek into the various configuration elements of the metadata to handle
 * further saml processing. A metadata adaptor is always linked to a saml service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class SamlRegisteredServiceServiceProviderMetadataFacade {

    private final SPSSODescriptor ssoDescriptor;

    private final EntityDescriptor entityDescriptor;

    @Getter
    private final MetadataResolver metadataResolver;

    /**
     * Adapt saml metadata and parse. Acts as a facade.
     *
     * @param resolver          the resolver
     * @param registeredService the service
     * @param entityID          the entity id
     * @return the saml metadata adaptor
     */
    public static Optional<SamlRegisteredServiceServiceProviderMetadataFacade> get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                                   final SamlRegisteredService registeredService,
                                                                                   final String entityID) {
        return get(resolver, registeredService, entityID, new CriteriaSet());
    }

    /**
     * Adapt saml metadata and parse. Acts as a facade.
     *
     * @param resolver          the resolver
     * @param registeredService the service
     * @param request           the request
     * @return the saml metadata adaptor
     */
    public static Optional<SamlRegisteredServiceServiceProviderMetadataFacade> get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                                   final SamlRegisteredService registeredService,
                                                                                   final RequestAbstractType request) {
        return get(resolver, registeredService, SamlIdPUtils.getIssuerFromSamlObject(request));
    }

    @SneakyThrows
    private static Optional<SamlRegisteredServiceServiceProviderMetadataFacade> get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                                    final SamlRegisteredService registeredService,
                                                                                    final String entityID,
                                                                                    final CriteriaSet criterions) {
        LOGGER.trace("Adapting SAML metadata for CAS service [{}] issued by [{}]", registeredService.getName(), entityID);
        criterions.add(new EntityIdCriterion(entityID), true);
        LOGGER.debug("Locating metadata for entityID [{}] by attempting to run through the metadata chain...", entityID);
        val chainingMetadataResolver = resolver.resolve(registeredService, criterions);
        LOGGER.info("Resolved metadata chain from [{}]. Filtering the chain by entity ID [{}]",
            registeredService.getMetadataLocation(), entityID);

        val entityDescriptor = chainingMetadataResolver.resolveSingle(criterions);
        if (entityDescriptor == null) {
            LOGGER.warn("Cannot find entity [{}] in metadata provider Ensure the metadata is valid and has not expired.", entityID);
            return Optional.empty();
        }
        LOGGER.trace("Located entity descriptor in metadata for [{}]", entityID);

        if (entityDescriptor.getValidUntil() != null) {
            val expired = entityDescriptor.getValidUntil()
                .isBefore(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
            if (expired) {
                LOGGER.warn("Entity descriptor in the metadata has expired at [{}]", entityDescriptor.getValidUntil());
                return Optional.empty();
            }
        }
        return getServiceProviderSsoDescriptor(entityID, chainingMetadataResolver, entityDescriptor);
    }

    private static Optional<SamlRegisteredServiceServiceProviderMetadataFacade>
        getServiceProviderSsoDescriptor(final String entityID,
                                    final MetadataResolver chainingMetadataResolver,
                                    final EntityDescriptor entityDescriptor) {
        val ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (ssoDescriptor != null) {
            LOGGER.debug("Located SP SSODescriptor in metadata for [{}]. Metadata is valid until [{}]", entityID,
                ObjectUtils.defaultIfNull(ssoDescriptor.getValidUntil(), "forever"));
            if (ssoDescriptor.getValidUntil() != null) {
                val validUntil = DateTimeUtils.zonedDateTimeOf(ssoDescriptor.getValidUntil());
                val expired = validUntil.isBefore(ZonedDateTime.now(ZoneOffset.UTC));
                if (expired) {
                    LOGGER.warn("SP SSODescriptor in the metadata has expired at [{}]", ssoDescriptor.getValidUntil());
                    return Optional.empty();
                }
            }
            return Optional.of(new SamlRegisteredServiceServiceProviderMetadataFacade(ssoDescriptor, entityDescriptor,
                chainingMetadataResolver));
        }
        LOGGER.warn("Could not locate SP SSODescriptor in the metadata for [{}]", entityID);
        return Optional.empty();
    }

    public ZonedDateTime getValidUntil() {
        return DateTimeUtils.zonedDateTimeOf(this.ssoDescriptor.getValidUntil());
    }

    public Organization getOrganization() {
        return this.ssoDescriptor.getOrganization();
    }

    public Signature getSignature() {
        return this.ssoDescriptor.getSignature();
    }

    public List<ContactPerson> getContactPersons() {
        return this.ssoDescriptor.getContactPersons();
    }

    public Duration getCacheDuration() {
        return this.ssoDescriptor.getCacheDuration();
    }

    public List<KeyDescriptor> getKeyDescriptors() {
        return this.ssoDescriptor.getKeyDescriptors();
    }

    public Extensions getExtensions() {
        return this.ssoDescriptor.getExtensions();
    }

    public List<String> getSupportedProtocols() {
        return this.ssoDescriptor.getSupportedProtocols();
    }

    public boolean isWantAssertionsSigned() {
        return this.ssoDescriptor.getWantAssertionsSigned();
    }

    public boolean isAuthnRequestsSigned() {
        return this.ssoDescriptor.isAuthnRequestsSigned();
    }

    /**
     * Is supported protocol?
     *
     * @param protocol the protocol
     * @return true/false
     */
    public boolean isSupportedProtocol(final String protocol) {
        return this.ssoDescriptor.isSupportedProtocol(protocol);
    }

    /**
     * Gets entity id.
     *
     * @return the entity id
     */
    public String getEntityId() {
        return this.entityDescriptor.getEntityID();
    }

    /**
     * Gets supported name formats.
     *
     * @return the supported name formats
     */
    public List<String> getSupportedNameIdFormats() {
        val nameIdFormats = new ArrayList<String>();
        val children = this.ssoDescriptor.getOrderedChildren();
        if (children != null) {
            nameIdFormats.addAll(children.stream().filter(NameIDFormat.class::isInstance)
                .map(child -> ((NameIDFormat) child).getURI()).collect(Collectors.toList()));
        }
        return nameIdFormats;
    }

    private List<AssertionConsumerService> getAssertionConsumerServices() {
        return (List) this.ssoDescriptor.getEndpoints(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    }

    public List<SingleLogoutService> getSingleLogoutServices() {
        return (List) this.ssoDescriptor.getEndpoints(SingleLogoutService.DEFAULT_ELEMENT_NAME);
    }

    public SingleLogoutService getSingleLogoutService() {
        return getSingleLogoutServices().get(0);
    }

    /**
     * Gets single logout service for the requested binding.
     *
     * @param binding the binding
     * @return the single logout service or null
     */
    public SingleLogoutService getSingleLogoutService(final String binding) {
        return getSingleLogoutServices().stream().filter(acs -> acs.getBinding().equalsIgnoreCase(binding)).findFirst().orElse(null);
    }

    /**
     * Gets assertion consumer service.
     *
     * @param binding the binding
     * @return the assertion consumer service
     */
    public AssertionConsumerService getAssertionConsumerService(final String binding) {
        return getAssertionConsumerServices().stream().filter(acs -> acs.getBinding().equalsIgnoreCase(binding)).findFirst().orElse(null);
    }

    /**
     * Gets assertion consumer service for paos binding.
     *
     * @return the assertion consumer service for paos binding
     */
    public AssertionConsumerService getAssertionConsumerServiceForPaosBinding() {
        return getAssertionConsumerService(SAMLConstants.SAML2_PAOS_BINDING_URI);
    }

    /**
     * Gets assertion consumer service for post binding.
     *
     * @return the assertion consumer service for post binding
     */
    public AssertionConsumerService getAssertionConsumerServiceForPostBinding() {
        return getAssertionConsumerService(SAMLConstants.SAML2_POST_BINDING_URI);
    }

    /**
     * Gets assertion consumer service for artifact binding.
     *
     * @return the assertion consumer service for artifact binding
     */
    public AssertionConsumerService getAssertionConsumerServiceForArtifactBinding() {
        return getAssertionConsumerService(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
    }


    /**
     * Contains assertion consumer services ?
     *
     * @return true/false
     */
    public boolean containsAssertionConsumerServices() {
        return !getAssertionConsumerServices().isEmpty();
    }

    /**
     * Assertion consumer services size.
     *
     * @return the size
     */
    public int assertionConsumerServicesSize() {
        return getAssertionConsumerServices().size();
    }
}
