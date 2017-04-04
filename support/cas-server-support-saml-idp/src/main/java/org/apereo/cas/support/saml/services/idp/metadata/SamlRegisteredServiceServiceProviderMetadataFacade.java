
package org.apereo.cas.support.saml.services.idp.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.DateTimeUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
public final class SamlRegisteredServiceServiceProviderMetadataFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlRegisteredServiceServiceProviderMetadataFacade.class);

    private final SPSSODescriptor ssoDescriptor;
    private final EntityDescriptor entityDescriptor;
    private final MetadataResolver metadataResolver;

    private SamlRegisteredServiceServiceProviderMetadataFacade(final SPSSODescriptor ssoDescriptor,
                                                               final EntityDescriptor entityDescriptor,
                                                               final MetadataResolver metadataResolver) {
        this.ssoDescriptor = ssoDescriptor;
        this.entityDescriptor = entityDescriptor;
        this.metadataResolver = metadataResolver;
    }


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
        return get(resolver, registeredService, SamlIdPUtils.getIssuerFromSamlRequest(request));
    }

    private static Optional<SamlRegisteredServiceServiceProviderMetadataFacade> get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                                    final SamlRegisteredService registeredService,
                                                                                    final String entityID,
                                                                                    final CriteriaSet criterions) {
        LOGGER.info("Adapting SAML metadata for CAS service [{}] issued by [{}]",
                registeredService.getName(), entityID);
        try {
            criterions.add(new BindingCriterion(Collections.singletonList(SAMLConstants.SAML2_POST_BINDING_URI)));
            criterions.add(new EntityIdCriterion(entityID));

            LOGGER.info("Locating metadata for entityID [{}] with binding [{}] by attempting to run through the metadata chain...",
                    entityID, SAMLConstants.SAML2_POST_BINDING_URI);
            final ChainingMetadataResolver chainingMetadataResolver = resolver.resolve(registeredService);
            LOGGER.info("Resolved metadata chain for service [{}]. Filtering the chain by entity ID [{}] and binding [{}]",
                    registeredService.getServiceId(), entityID, SAMLConstants.SAML2_POST_BINDING_URI);

            final EntityDescriptor entityDescriptor = chainingMetadataResolver.resolveSingle(criterions);
            if (entityDescriptor == null) {
                LOGGER.debug("Cannot find entity [{}] in metadata provider.", entityID);
                return Optional.empty();
            }
            LOGGER.debug("Located EntityDescriptor in metadata for [{}]", entityID);
            final SPSSODescriptor ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
            if (ssoDescriptor != null) {
                LOGGER.debug("Located SPSSODescriptor in metadata for [{}]. Metadata is valid until [{}]",
                        entityID, ssoDescriptor.getValidUntil());
                return Optional.of(new SamlRegisteredServiceServiceProviderMetadataFacade(ssoDescriptor, entityDescriptor, chainingMetadataResolver));
            }
            LOGGER.warn("Could not locate SPSSODescriptor in the metadata for [{}]", entityID);
            return Optional.empty();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SPSSODescriptor getSsoDescriptor() {
        return this.ssoDescriptor;
    }

    public ZonedDateTime getValidUntil() {
        return DateTimeUtils.zonedDateTimeOf(this.ssoDescriptor.getValidUntil());
    }

    public EntityDescriptor getEntityDescriptor() {
        return this.entityDescriptor;
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

    public long getCacheDuration() {
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
        final List<String> nameIdFormats = new ArrayList<>();
        final List<XMLObject> children = this.ssoDescriptor.getOrderedChildren();
        if (children != null) {
            nameIdFormats.addAll(children.stream().filter(NameIDFormat.class::isInstance)
                    .map(child -> ((NameIDFormat) child).getFormat()).collect(Collectors.toList()));
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
     * Gets assertion consumer service.
     *
     * @param binding the binding
     * @return the assertion consumer service
     */
    public AssertionConsumerService getAssertionConsumerService(final String binding) {
        return getAssertionConsumerServices().stream().filter(acs -> acs.getBinding().equals(binding)).findFirst().orElse(null);
    }

    private AssertionConsumerService getAssertionConsumerServiceForPaosBinding() {
        return getAssertionConsumerService(SAMLConstants.SAML2_PAOS_BINDING_URI);
    }

    private AssertionConsumerService getAssertionConsumerServiceForPostBinding() {
        return getAssertionConsumerService(SAMLConstants.SAML2_POST_BINDING_URI);
    }
        
    public MetadataResolver getMetadataResolver() {
        return this.metadataResolver;
    }

    /**
     * Contains assertion consumer services ?
     *
     * @return true/false
     */
    public boolean containsAssertionConsumerServices() {
        return !getAssertionConsumerServices().isEmpty();
    }
}
