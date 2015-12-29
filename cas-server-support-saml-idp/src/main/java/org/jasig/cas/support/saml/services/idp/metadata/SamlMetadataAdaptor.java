package org.jasig.cas.support.saml.services.idp.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.joda.time.DateTime;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EndpointCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.xmlsec.signature.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is {@link SamlMetadataAdaptor}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public final class SamlMetadataAdaptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlMetadataAdaptor.class);

    private final AssertionConsumerService assertionConsumerService;
    private final SPSSODescriptor ssoDescriptor;
    private final EntityDescriptor entityDescriptor;
    private final MetadataResolver metadataResolver;

    private SamlMetadataAdaptor(final AssertionConsumerService assertionConsumerService,
                                final SPSSODescriptor ssoDescriptor,
                                final EntityDescriptor entityDescriptor,
                                final MetadataResolver metadataResolver) {
        this.assertionConsumerService = assertionConsumerService;
        this.ssoDescriptor = ssoDescriptor;
        this.entityDescriptor = entityDescriptor;
        this.metadataResolver = metadataResolver;
    }

    /**
     * Adapt saml metadata and parse. Acts as a facade.
     *
     * @param resolver          the resolver
     * @param registeredService the service
     * @param authnRequest      the authn request
     * @return the saml metadata adaptor
     */
    public static SamlMetadataAdaptor adapt(final SamlRegisteredServiceCachingMetadataResolver resolver,
                                            final SamlRegisteredService registeredService,
                                            final AuthnRequest authnRequest) {
        try {
            final String issuer = authnRequest.getIssuer().getValue();
            LOGGER.info("Adapting SAML metadata for CAS service [{}] issued by [{}]",
                    registeredService.getName(), issuer);

            final AssertionConsumerService assertionConsumerService = getAssertionConsumerServiceFor(authnRequest);
            final CriteriaSet criterions = new CriteriaSet();
            criterions.add(new BindingCriterion(Collections.singletonList(SAMLConstants.SAML2_POST_BINDING_URI)));
            criterions.add(new EntityIdCriterion(issuer));
            criterions.add(new EndpointCriterion<>(assertionConsumerService, true));

            LOGGER.info("Locating metadata for entityID [{}], with binding [{}] and ACS endpoint [{}]",
                    issuer, SAMLConstants.SAML2_POST_BINDING_URI, assertionConsumerService.getLocation());
            final ChainingMetadataResolver chainingMetadataResolver = resolver.resolve(registeredService);
            final EntityDescriptor entityDescriptor = chainingMetadataResolver.resolveSingle(criterions);
            if (entityDescriptor == null) {
                throw new SAMLException("Cannot find entity " + assertionConsumerService.getLocation() + " in metadata provider.");
            }
            LOGGER.debug("Located EntityDescriptor in metadata for [{}]", issuer);
            final SPSSODescriptor ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
            if (ssoDescriptor != null) {
                LOGGER.debug("Located SPSSODescriptor in metadata for [{}]. Metadata is valid until [{}]",
                        issuer, ssoDescriptor.getValidUntil());
                return new SamlMetadataAdaptor(assertionConsumerService, ssoDescriptor, entityDescriptor, chainingMetadataResolver);
            }
            throw new SamlException("Could not locate SPSSODescriptor in the metadata for " + issuer);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public AssertionConsumerService getAssertionConsumerServiceProvided() {
        return assertionConsumerService;
    }

    public SPSSODescriptor getSsoDescriptor() {
        return this.ssoDescriptor;
    }

    public DateTime getValidUntil() {
        return this.ssoDescriptor.getValidUntil();
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
            for (final XMLObject child : children) {
                if (child instanceof NameIDFormat) {
                    nameIdFormats.add(((NameIDFormat) child).getFormat());
                }
            }
        }
        return nameIdFormats;
    }

    public List<AssertionConsumerService> getAssertionConsumerServices() {
        return (List) this.ssoDescriptor.getEndpoints(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    }

    public AssertionConsumerService getAssertionConsumerService() {
        return getAssertionConsumerServices().get(0);
    }

    public MetadataResolver getMetadataResolver() {
        return metadataResolver;
    }

    /**
     * Gets assertion consumer service for.
     *
     * @param authnRequest the authn request
     * @return the assertion consumer service for
     */
    private static AssertionConsumerService getAssertionConsumerServiceFor(final AuthnRequest authnRequest) {
        final AssertionConsumerService acs = new AssertionConsumerServiceBuilder().buildObject();
        acs.setBinding(authnRequest.getProtocolBinding());
        acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
        acs.setResponseLocation(authnRequest.getAssertionConsumerServiceURL());
        return acs;
    }
}
