package org.jasig.cas.support.saml.services.idp.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EndpointCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.xmlsec.signature.Signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link SamlMetadataAdaptor}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public final class SamlMetadataAdaptor {
    private final AssertionConsumerService assertionConsumerService;
    private final SSODescriptor ssoDescriptor;

    private SamlMetadataAdaptor(final AssertionConsumerService assertionConsumerService,
                                final SSODescriptor ssoDescriptor) {
        this.assertionConsumerService = assertionConsumerService;
        this.ssoDescriptor = ssoDescriptor;
    }

    /**
     * Adapt saml metadata and parse. Acts as a facade.
     *
     * @param metadataResolver         the metadata resolver
     * @param assertionConsumerService the assertion consumer service
     * @return the saml metadata adaptor
     */
    public static SamlMetadataAdaptor adapt(final ChainingMetadataResolver metadataResolver,
                                            final AssertionConsumerService assertionConsumerService) {
        try {
            final CriteriaSet criterions = new CriteriaSet();
            criterions.add(new BindingCriterion(Arrays.asList(SAMLConstants.SAML2_POST_BINDING_URI)));
            criterions.add(new EndpointCriterion<>(assertionConsumerService, true));

            final EntityDescriptor entityDescriptor = metadataResolver.resolveSingle(criterions);
            if (entityDescriptor == null) {
                throw new SAMLException("Cannot find entity " + assertionConsumerService.getLocation() + " in metadata provider");
            }
            final SSODescriptor ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
            return new SamlMetadataAdaptor(assertionConsumerService, ssoDescriptor);

        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public AssertionConsumerService getAssertionConsumerService() {
        return assertionConsumerService;
    }

    public SSODescriptor getSsoDescriptor() {
        return this.ssoDescriptor;
    }

    public DateTime getValidUntil() {
        return this.ssoDescriptor.getValidUntil();
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

}
