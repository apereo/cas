package org.apereo.cas.support.saml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link SamlIdPUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class SamlIdPUtils {

    /**
     * Prepare peer entity saml endpoint.
     *
     * @param authnRequest    the authn request
     * @param outboundContext the outbound context
     * @param adaptor         the adaptor
     * @param binding         the binding
     * @throws SamlException the saml exception
     */
    public static void preparePeerEntitySamlEndpointContext(final RequestAbstractType authnRequest,
                                                            final MessageContext outboundContext,
                                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                            final String binding) throws SamlException {
        final var entityId = adaptor.getEntityId();
        if (!adaptor.containsAssertionConsumerServices()) {
            throw new SamlException("No assertion consumer service could be found for entity " + entityId);
        }

        final var peerEntityContext = outboundContext.getSubcontext(SAMLPeerEntityContext.class, true);
        if (peerEntityContext == null) {
            throw new SamlException("SAMLPeerEntityContext could not be defined for entity " + entityId);
        }
        peerEntityContext.setEntityId(entityId);

        final var endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
        if (endpointContext == null) {
            throw new SamlException("SAMLEndpointContext could not be defined for entity " + entityId);
        }

        final Endpoint endpoint = determineAssertionConsumerService(authnRequest, adaptor, binding);
        LOGGER.debug("Configured peer entity endpoint to be [{}] with binding [{}]", endpoint.getLocation(), endpoint.getBinding());
        endpointContext.setEndpoint(endpoint);
    }

    /**
     * Determine assertion consumer service assertion consumer service.
     *
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @param binding      the binding
     * @return the assertion consumer service
     */
    public static AssertionConsumerService determineAssertionConsumerService(final RequestAbstractType authnRequest,
                                                                             final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                             final String binding) {
        AssertionConsumerService endpoint = null;

        if (authnRequest instanceof AuthnRequest) {
            final var acsUrl = AuthnRequest.class.cast(authnRequest).getAssertionConsumerServiceURL();
            if (StringUtils.isNotBlank(acsUrl)) {
                LOGGER.debug("Using assertion consumer service url [{}] with binding [{}] provided by the authentication request", acsUrl, binding);
                final var builder = new AssertionConsumerServiceBuilder();
                endpoint = builder.buildObject(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
                endpoint.setBinding(binding);
                endpoint.setResponseLocation(acsUrl);
                endpoint.setLocation(acsUrl);
            }
        }

        if (endpoint == null) {
            LOGGER.debug("Attempting to locate the assertion consumer service url for binding [{}] from metadata", binding);
            endpoint = adaptor.getAssertionConsumerService(binding);
        }
        if (StringUtils.isBlank(endpoint.getBinding()) || StringUtils.isBlank(endpoint.getLocation())) {
            throw new SamlException("Assertion consumer service does not define a binding or location");
        }
        return endpoint;
    }

    /**
     * Gets chaining metadata resolver for all saml services.
     *
     * @param servicesManager the services manager
     * @param entityID        the entity id
     * @param resolver        the resolver
     * @return the chaining metadata resolver for all saml services
     */
    @SneakyThrows
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public static MetadataResolver getMetadataResolverForAllSamlServices(final ServicesManager servicesManager,
                                                                         final String entityID,
                                                                         final SamlRegisteredServiceCachingMetadataResolver resolver) {

        final var registeredServices = servicesManager.findServiceBy(SamlRegisteredService.class::isInstance);
        final var chainingMetadataResolver = new ChainingMetadataResolver();

        final var resolvers = registeredServices.stream()
            .filter(SamlRegisteredService.class::isInstance)
            .map(SamlRegisteredService.class::cast)
            .map(s -> SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, s, entityID))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(SamlRegisteredServiceServiceProviderMetadataFacade::getMetadataResolver)
            .collect(Collectors.toList());

        LOGGER.debug("Located [{}] metadata resolvers to match against [{}]", resolvers, entityID);

        chainingMetadataResolver.setResolvers(resolvers);
        chainingMetadataResolver.setId(entityID);
        chainingMetadataResolver.initialize();
        return chainingMetadataResolver;
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
                                                                          final ServicesManager servicesManager,
                                                                          final SamlRegisteredServiceCachingMetadataResolver resolver) {
        try {
            final var acs = new AssertionConsumerServiceBuilder().buildObject();
            if (authnRequest.getAssertionConsumerServiceIndex() != null) {
                final var issuer = getIssuerFromSamlRequest(authnRequest);
                final var samlResolver = getMetadataResolverForAllSamlServices(servicesManager, issuer, resolver);
                final var criteriaSet = new CriteriaSet();
                criteriaSet.add(new EntityIdCriterion(issuer));
                criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
                criteriaSet.add(new BindingCriterion(CollectionUtils.wrap(SAMLConstants.SAML2_POST_BINDING_URI)));

                final var it = samlResolver.resolve(criteriaSet);
                it.forEach(entityDescriptor -> {
                    final var spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
                    final var acsEndpoints = spssoDescriptor.getAssertionConsumerServices();
                    if (acsEndpoints.isEmpty()) {
                        throw new IllegalArgumentException("Metadata resolved for entity id " + issuer + " has no defined ACS endpoints");
                    }
                    final int acsIndex = authnRequest.getAssertionConsumerServiceIndex();
                    if (acsIndex + 1 > acsEndpoints.size()) {
                        throw new IllegalArgumentException("AssertionConsumerService index specified in the request " + acsIndex + " is invalid "
                            + "since the total endpoints available to " + issuer + " is " + acsEndpoints.size());
                    }
                    final var foundAcs = acsEndpoints.get(acsIndex);
                    acs.setBinding(foundAcs.getBinding());
                    acs.setLocation(foundAcs.getLocation());
                    acs.setResponseLocation(foundAcs.getResponseLocation());
                    acs.setIndex(acsIndex);
                });
            } else {
                acs.setBinding(authnRequest.getProtocolBinding());
                acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
                acs.setResponseLocation(authnRequest.getAssertionConsumerServiceURL());
                acs.setIndex(0);
                acs.setIsDefault(Boolean.TRUE);
            }

            LOGGER.debug("Resolved AssertionConsumerService from the request is [{}]", acs);
            if (StringUtils.isBlank(acs.getBinding())) {
                throw new SamlException("AssertionConsumerService has no protocol binding defined");
            }
            if (StringUtils.isBlank(acs.getLocation()) && StringUtils.isBlank(acs.getResponseLocation())) {
                throw new SamlException("AssertionConsumerService has no location or response location defined");
            }
            return acs;
        } catch (final Exception e) {
            throw new IllegalArgumentException(new SamlException(e.getMessage(), e));
        }
    }

    /**
     * Gets issuer from saml request.
     *
     * @param request the request
     * @return the issuer from saml request
     */
    public static String getIssuerFromSamlRequest(final RequestAbstractType request) {
        return request.getIssuer().getValue();
    }

    /**
     * Gets role descriptor resolver.
     *
     * @param adaptor              the adaptor
     * @param requireValidMetadata the require valid metadata
     * @return the role descriptor resolver
     * @throws Exception the exception
     */
    public static RoleDescriptorResolver getRoleDescriptorResolver(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                   final boolean requireValidMetadata) throws Exception {
        return getRoleDescriptorResolver(adaptor.getMetadataResolver(), requireValidMetadata);
    }

    /**
     * Gets role descriptor resolver.
     *
     * @param metadata             the metadata
     * @param requireValidMetadata the require valid metadata
     * @return the role descriptor resolver
     * @throws Exception the exception
     */
    public static RoleDescriptorResolver getRoleDescriptorResolver(final MetadataResolver metadata,
                                                                   final boolean requireValidMetadata) throws Exception {
        final var roleDescriptorResolver = new PredicateRoleDescriptorResolver(metadata);
        roleDescriptorResolver.setSatisfyAnyPredicates(true);
        roleDescriptorResolver.setUseDefaultPredicateRegistry(true);
        roleDescriptorResolver.setRequireValidMetadata(requireValidMetadata);
        roleDescriptorResolver.initialize();
        return roleDescriptorResolver;
    }
}


