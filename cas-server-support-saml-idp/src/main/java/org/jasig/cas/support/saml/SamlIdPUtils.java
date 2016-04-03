package org.jasig.cas.support.saml;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.cryptacular.util.CertUtil;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link SamlIdPUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class SamlIdPUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlIdPUtils.class);

    private SamlIdPUtils() {
    }

    /**
     * Read certificate x 509 certificate.
     *
     * @param resource the resource
     * @return the x 509 certificate
     */
    public static X509Certificate readCertificate(final Resource resource) {
        try (final InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new RuntimeException("Error reading certificate " + resource, e);
        }
    }

    /**
     * Transform saml object to String.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @return the string
     * @throws SamlException the saml exception
     */
    public static StringWriter transformSamlObject(final OpenSamlConfigBean configBean, final SAMLObject samlObject) throws SamlException {
        final StringWriter writer = new StringWriter();
        try {
            final Marshaller marshaller = configBean.getMarshallerFactory().getMarshaller(samlObject.getElementQName());
            if (marshaller != null) {
                final Element element = marshaller.marshall(samlObject);
                final DOMSource domSource = new DOMSource(element);
                
                final StreamResult result = new StreamResult(writer);
                final TransformerFactory tf = TransformerFactory.newInstance();
                final Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);
            }
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
        return writer;
    }
    
    /**
     * Log saml object.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @throws SamlException the saml exception
     */
    public static void logSamlObject(final OpenSamlConfigBean configBean, final SAMLObject samlObject) throws SamlException {
        LOGGER.debug("Logging [{}]\n{}", samlObject.getClass().getName(), transformSamlObject(configBean, samlObject));
    }

    /**
     * Prepare peer entity saml endpoint.
     *
     * @param outboundContext the outbound context
     * @param adaptor         the adaptor
     * @throws SamlException the saml exception
     */
    public static void preparePeerEntitySamlEndpointContext(final MessageContext outboundContext,
                                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        final List<AssertionConsumerService> assertionConsumerServices = adaptor.getAssertionConsumerServices();
        if (assertionConsumerServices.isEmpty()) {
            throw new SamlException(SamlException.CODE, "No assertion consumer service could be found for entity " + adaptor.getEntityId());
        }

        final SAMLPeerEntityContext peerEntityContext = outboundContext.getSubcontext(SAMLPeerEntityContext.class, true);
        if (peerEntityContext == null) {
            throw new SamlException(SamlException.CODE, "SAMLPeerEntityContext could not be defined for entity " + adaptor.getEntityId());
        }

        final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
        if (endpointContext == null) {
            throw new SamlException(SamlException.CODE, "SAMLEndpointContext could not be defined for entity " + adaptor.getEntityId());
        }
        final Endpoint endpoint = assertionConsumerServices.get(0);
        if (StringUtils.isBlank(endpoint.getBinding()) || StringUtils.isBlank(endpoint.getLocation())) {
            throw new SamlException(SamlException.CODE, "Assertion consumer service does not define a binding or location for "
                    + adaptor.getEntityId());
        }
        LOGGER.debug("Configured peer entity endpoint to be [{}] with binding [{}]", endpoint.getLocation(), endpoint.getBinding());
        endpointContext.setEndpoint(endpoint);
    }

    /**
     * Gets chaining metadata resolver for all saml services.
     *
     * @param servicesManager the services manager
     * @param entityID        the entity id
     * @param resolver        the resolver
     * @return the chaining metadata resolver for all saml services
     */
    public static MetadataResolver getMetadataResolverForAllSamlServices(final ServicesManager servicesManager,
                                         final String entityID, final SamlRegisteredServiceCachingMetadataResolver resolver) {
        try {
            final Predicate p = Predicates.instanceOf(SamlRegisteredService.class);
            final Collection<RegisteredService> registeredServices = servicesManager.findServiceBy(p);
            final List<MetadataResolver> resolvers = new ArrayList<>();
            final ChainingMetadataResolver chainingMetadataResolver = new ChainingMetadataResolver();

            for (final RegisteredService registeredService : registeredServices) {
                final SamlRegisteredService samlRegisteredService = SamlRegisteredService.class.cast(registeredService);

                final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                        SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlRegisteredService, entityID);
                resolvers.add(adaptor.getMetadataResolver());
            }
            chainingMetadataResolver.setResolvers(resolvers);
            chainingMetadataResolver.setId(entityID);
            chainingMetadataResolver.initialize();
            return chainingMetadataResolver;
        } catch (final Exception e) {
            throw new RuntimeException(new SamlException(e.getMessage(), e));
        }
    }

    /**
     * Gets assertion consumer service for.
     *
     * @param authnRequest    the authn request
     * @param servicesManager the services manager
     * @param resolver        the resolver
     * @return the assertion consumer service for
     */
    public static AssertionConsumerService getAssertionConsumerServiceFor(final AuthnRequest authnRequest, 
                final ServicesManager servicesManager, final SamlRegisteredServiceCachingMetadataResolver resolver) {
        try {
            final AssertionConsumerService acs = new AssertionConsumerServiceBuilder().buildObject();
            if (authnRequest.getAssertionConsumerServiceIndex() != null) {
                final String issuer = authnRequest.getIssuer().getValue();
                final MetadataResolver samlResolver = getMetadataResolverForAllSamlServices(servicesManager, issuer, resolver);
                final CriteriaSet criteriaSet = new CriteriaSet();
                criteriaSet.add(new EntityIdCriterion(issuer));
                criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
                criteriaSet.add(new BindingCriterion(Arrays.asList(SAMLConstants.SAML2_POST_BINDING_URI)));
                
                final Iterable<EntityDescriptor> it = samlResolver.resolve(criteriaSet);
                it.forEach(entityDescriptor -> {
                    final SPSSODescriptor spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
                    final List<AssertionConsumerService> acsEndpoints = spssoDescriptor.getAssertionConsumerServices();
                    if (acsEndpoints.isEmpty()) {
                        throw new RuntimeException("Metadata resolved for entity id " + issuer + " has no defined ACS endpoints");
                    }
                    final int acsIndex = authnRequest.getAssertionConsumerServiceIndex();
                    if (acsIndex + 1 > acsEndpoints.size()) {
                        throw new RuntimeException("AssertionConsumerService index specified in the request " + acsIndex + " is invalid "
                                + "since the total endpoints available to " + acsIndex + " is " + acsEndpoints.size());
                    }
                    final AssertionConsumerService foundAcs = acsEndpoints.get(acsIndex);
                    acs.setBinding(foundAcs.getBinding());
                    acs.setLocation(foundAcs.getLocation());
                    acs.setResponseLocation(foundAcs.getResponseLocation());
                    acs.setIndex(acsIndex);
                    return;
                });
            } else {
                acs.setBinding(authnRequest.getProtocolBinding());
                acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
                acs.setResponseLocation(authnRequest.getAssertionConsumerServiceURL());
                acs.setIndex(0);
                acs.setIsDefault(true);
            }
            
            LOGGER.debug("Resolve AssertionConsumerService from the request is {}", acs);
            if (StringUtils.isBlank(acs.getBinding())) {
                throw new SamlException("AssertionConsumerService has no protocol binding defined");
            }
            if (StringUtils.isBlank(acs.getLocation()) && StringUtils.isBlank(acs.getResponseLocation())) {
                throw new SamlException("AssertionConsumerService has no location defined");
            }
            return acs;
        } catch (final Exception e) {
            throw new RuntimeException(new SamlException(e.getMessage(), e));
        }
    }
}


