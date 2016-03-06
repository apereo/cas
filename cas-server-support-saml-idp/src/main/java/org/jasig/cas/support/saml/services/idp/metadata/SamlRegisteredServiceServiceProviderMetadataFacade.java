
package org.jasig.cas.support.saml.services.idp.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.SamlIdPUtils;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.jasig.cas.util.DateTimeUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EndpointCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
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
import java.util.stream.Collectors;

/**
 * This is {@link SamlRegisteredServiceServiceProviderMetadataFacade} that acts a façade between the SAML metadata resolved
 * from a metadata resource and other outer layers in CAS that need access to the bits of that
 * metadata. Once the metadata resolved for a service is adapted and parsed for a given entity id,
 * callers will be able to peek into the various configuration elements of the metadata to handle
 * further saml processing. A metadata adaptor is always linked to a saml service.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
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
    public static SamlRegisteredServiceServiceProviderMetadataFacade get(final SamlRegisteredServiceCachingMetadataResolver resolver,
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
    public static SamlRegisteredServiceServiceProviderMetadataFacade get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                         final SamlRegisteredService registeredService,
                                                                         final RequestAbstractType request) {
        return get(resolver, registeredService, request.getIssuer().getValue());
    }

    /**
     * Adapt saml metadata and parse. Acts as a facade.
     *
     * @param resolver          the resolver
     * @param registeredService the service
     * @param authnRequest      the authn request
     * @return the saml metadata adaptor
     */
    public static SamlRegisteredServiceServiceProviderMetadataFacade get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                          final SamlRegisteredService registeredService,
                                                                          final AuthnRequest authnRequest) {
        try {
            final AssertionConsumerService assertionConsumerService = SamlIdPUtils.getAssertionConsumerServiceFor(authnRequest);
            final CriteriaSet criterions = new CriteriaSet();
            criterions.add(new EndpointCriterion<>(assertionConsumerService, true));
            LOGGER.info("Locating metadata for entityID [{}], with binding [{}] and ACS endpoint [{}]",
                    authnRequest.getIssuer().getValue(), SAMLConstants.SAML2_POST_BINDING_URI,
                    assertionConsumerService.getLocation());
            return get(resolver, registeredService, authnRequest.getIssuer().getValue(), criterions);

        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static SamlRegisteredServiceServiceProviderMetadataFacade get(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                         final SamlRegisteredService registeredService,
                                                                         final String entityID,
                                                                         final CriteriaSet criterions) {
        try {
            criterions.add(new BindingCriterion(Collections.singletonList(SAMLConstants.SAML2_POST_BINDING_URI)));
            criterions.add(new EntityIdCriterion(entityID));

            LOGGER.info("Adapting SAML metadata for CAS service [{}] issued by [{}]",
                    registeredService.getName(), entityID);
            LOGGER.info("Locating metadata for entityID [{}] with binding [{}]",
                    entityID, SAMLConstants.SAML2_POST_BINDING_URI);
            final ChainingMetadataResolver chainingMetadataResolver = resolver.resolve(registeredService);
            final EntityDescriptor entityDescriptor = chainingMetadataResolver.resolveSingle(criterions);
            if (entityDescriptor == null) {
                throw new SAMLException("Cannot find entity " + entityID + " in metadata provider.");
            }
            LOGGER.debug("Located EntityDescriptor in metadata for [{}]", entityID);
            final SPSSODescriptor ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
            if (ssoDescriptor != null) {
                LOGGER.debug("Located SPSSODescriptor in metadata for [{}]. Metadata is valid until [{}]",
                        entityID, ssoDescriptor.getValidUntil());
                return new SamlRegisteredServiceServiceProviderMetadataFacade(ssoDescriptor, entityDescriptor, chainingMetadataResolver);
            }
            throw new SamlException("Could not locate SPSSODescriptor in the metadata for " + entityID);
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
        return entityDescriptor;
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
    public List<String> getSupportedNameFormats() {
        final List<String> nameIdFormats = new ArrayList<>();
        final List<XMLObject> children = this.ssoDescriptor.getOrderedChildren();
        if (children != null) {
            nameIdFormats.addAll(children.stream().filter(child -> child instanceof NameIDFormat)
                    .map(child -> ((NameIDFormat) child).getFormat()).collect(Collectors.toList()));
        }
        return nameIdFormats;
    }

    public List<AssertionConsumerService> getAssertionConsumerServices() {
        return (List) this.ssoDescriptor.getEndpoints(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    }

    public List<SingleLogoutService> getSingleLogoutServices() {
        return (List) this.ssoDescriptor.getEndpoints(SingleLogoutService.DEFAULT_ELEMENT_NAME);
    }

    public SingleLogoutService getSingleLogoutService() {
        return getSingleLogoutServices().get(0);
    }

    public AssertionConsumerService getAssertionConsumerService() {
        return getAssertionConsumerServices().get(0);
    }

    public MetadataResolver getMetadataResolver() {
        return metadataResolver;
    }


}
