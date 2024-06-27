package org.apereo.cas.support.saml;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.MetadataEntityAttributeQuery;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import java.util.List;
import java.util.Objects;
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
     * Gets saml idp metadata owner.
     *
     * @param result the result
     * @return the saml id p metadata owner
     */
    public static String getSamlIdPMetadataOwner(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }


    /**
     * Prepare peer entity saml endpoint.
     *
     * @param authnContext    the authn context
     * @param outboundContext the outbound context
     * @param adaptor         the adaptor
     * @param binding         the binding
     * @throws SamlException the saml exception
     */
    public static void preparePeerEntitySamlEndpointContext(final Pair<? extends RequestAbstractType, MessageContext> authnContext,
                                                            final MessageContext outboundContext,
                                                            final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                            final String binding) throws SamlException {
        val entityId = adaptor.getEntityId();
        if (!adaptor.containsAssertionConsumerServices()) {
            throw new SamlException("No assertion consumer service could be found for entity " + entityId);
        }

        val peerEntityContext = outboundContext.ensureSubcontext(SAMLPeerEntityContext.class);
        peerEntityContext.setEntityId(entityId);

        val endpointContext = peerEntityContext.ensureSubcontext(SAMLEndpointContext.class);
        val endpoint = determineEndpointForRequest(authnContext, adaptor, binding);
        LOGGER.debug("Configured peer entity endpoint to be [{}] with binding [{}]", endpoint.getLocation(), endpoint.getBinding());
        endpointContext.setEndpoint(endpoint);
    }

    /**
     * Determine assertion consumer service assertion consumer service.
     *
     * @param authnContext the authn context
     * @param adaptor      the adaptor
     * @param binding      the binding
     * @return the assertion consumer service
     */
    public static Endpoint determineEndpointForRequest(final Pair<? extends RequestAbstractType, MessageContext> authnContext,
                                                       final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                       final String binding) {
        var endpoint = (Endpoint) null;
        val authnRequest = authnContext.getLeft();
        if (authnRequest instanceof LogoutRequest) {
            endpoint = adaptor.getSingleLogoutService(binding);
        } else {
            val acsEndpointFromReq = getAssertionConsumerServiceFromRequest(authnRequest, binding, adaptor);
            val acsEndpointFromMetadata = adaptor.getAssertionConsumerService(binding);
            endpoint = determineEndpointForRequest(authnRequest, adaptor, binding,
                acsEndpointFromReq, acsEndpointFromMetadata, authnContext.getRight());
        }
        if (endpoint == null) {
            throw new SamlException("Endpoint for " + authnRequest.getSchemaType()
                + " is not available or does not define a binding for " + binding);
        }
        val missingLocation = StringUtils.isBlank(endpoint.getResponseLocation()) && StringUtils.isBlank(endpoint.getLocation());
        if (StringUtils.isBlank(endpoint.getBinding()) || missingLocation) {
            throw new SamlException("Endpoint for " + authnRequest.getSchemaType()
                + " does not define a binding or location for binding " + binding);
        }
        return endpoint;
    }

    private static AssertionConsumerService determineEndpointForRequest(final RequestAbstractType authnRequest,
                                                                        final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                                        final String binding,
                                                                        final AssertionConsumerService acsFromRequest,
                                                                        final AssertionConsumerService acsFromMetadata,
                                                                        final MessageContext authenticationContext) {
        LOGGER.trace("ACS from authentication request is [{}], ACS from metadata is [{}] with binding [{}]",
            acsFromRequest, acsFromMetadata, binding);

        if (acsFromRequest != null) {
            if (!authnRequest.isSigned() && !SAMLBindingSupport.isMessageSigned(authenticationContext)) {
                val locations = StringUtils.isNotBlank(binding)
                    ? adaptor.getAssertionConsumerServiceLocations(binding)
                    : adaptor.getAssertionConsumerServiceLocations();
                val acsUrl = StringUtils.defaultIfBlank(acsFromRequest.getResponseLocation(), acsFromRequest.getLocation());
                val acsIndex = authnRequest instanceof AuthnRequest
                    ? ((AuthnRequest) authnRequest).getAssertionConsumerServiceIndex()
                    : null;

                if (StringUtils.isNotBlank(acsUrl) && locations.stream().anyMatch(acsUrl::equalsIgnoreCase)) {
                    return buildAssertionConsumerService(binding, acsUrl, acsIndex);
                }

                if (acsIndex != null) {
                    val result = adaptor.getAssertionConsumerServiceFor(binding, acsIndex);
                    if (result.isPresent()) {
                        return buildAssertionConsumerService(binding, result.get(), acsIndex);
                    }
                }
                val message = String.format("Assertion consumer service [%s] cannot be located in metadata [%s]", acsUrl, locations);
                throw new SamlException(message);
            }
            return acsFromRequest;
        }
        return acsFromMetadata;
    }

    private static AssertionConsumerService buildAssertionConsumerService(final String binding, final String acsUrl, final Integer acsIndex) {
        val acs = new AssertionConsumerServiceBuilder().buildObject();
        acs.setBinding(binding);
        acs.setLocation(acsUrl);
        acs.setResponseLocation(acsUrl);
        acs.setIndex(acsIndex);
        acs.setIsDefault(Boolean.TRUE);
        return acs;
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
                                                                         final String entityID,
                                                                         final SamlRegisteredServiceCachingMetadataResolver resolver) {

        val registeredServices = servicesManager.findServiceBy(SamlRegisteredService.class::isInstance);
        val chainingMetadataResolver = new ChainingMetadataResolver();

        val resolvers = registeredServices.stream()
            .filter(SamlRegisteredService.class::isInstance)
            .map(SamlRegisteredService.class::cast)
            .map(service -> SamlRegisteredServiceMetadataAdaptor.get(resolver, service, entityID))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(SamlRegisteredServiceMetadataAdaptor::getMetadataResolver)
            .collect(Collectors.toList());

        LOGGER.debug("Located [{}] metadata resolvers to match against [{}]", resolvers, entityID);

        FunctionUtils.doUnchecked(__ -> {
            chainingMetadataResolver.setResolvers(resolvers);
            chainingMetadataResolver.setId(entityID);
            chainingMetadataResolver.initialize();
        });
        return chainingMetadataResolver;
    }

    /**
     * Gets issuer from saml object.
     *
     * @param object the object
     * @return the issuer from saml object
     */
    public static String getIssuerFromSamlObject(final SAMLObject object) {
        if (object instanceof final RequestAbstractType instance) {
            return instance.getIssuer().getValue();
        }
        if (object instanceof final StatusResponseType instance) {
            return instance.getIssuer().getValue();
        }
        if (object instanceof final Assertion instance) {
            return instance.getIssuer().getValue();
        }
        return null;
    }

    /**
     * Gets role descriptor resolver.
     *
     * @param adaptor              the adaptor
     * @param requireValidMetadata the require valid metadata
     * @return the role descriptor resolver
     * @throws Exception the exception
     */
    public static RoleDescriptorResolver getRoleDescriptorResolver(final SamlRegisteredServiceMetadataAdaptor adaptor,
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
        val roleDescriptorResolver = new PredicateRoleDescriptorResolver(metadata);
        roleDescriptorResolver.setSatisfyAnyPredicates(true);
        roleDescriptorResolver.setUseDefaultPredicateRegistry(true);
        roleDescriptorResolver.setRequireValidMetadata(requireValidMetadata);
        roleDescriptorResolver.initialize();
        return roleDescriptorResolver;
    }

    /**
     * Gets name id policy.
     *
     * @param authnRequest the authn request
     * @return the name id policy
     */
    public static Optional<NameIDPolicy> getNameIDPolicy(final RequestAbstractType authnRequest) {
        if (authnRequest instanceof final AuthnRequest instance) {
            return Optional.ofNullable(instance.getNameIDPolicy());
        }
        return Optional.empty();
    }

    private static AssertionConsumerService getAssertionConsumerServiceFromRequest(final RequestAbstractType request,
                                                                                   final String binding,
                                                                                   final SamlRegisteredServiceMetadataAdaptor adapter) {
        if (request instanceof final AuthnRequest authnRequest) {
            var acsUrl = authnRequest.getAssertionConsumerServiceURL();
            val acsIndex = authnRequest.getAssertionConsumerServiceIndex();
            if (StringUtils.isBlank(acsUrl) && acsIndex == null) {
                LOGGER.debug("No assertion consumer service url or index is supplied in the authentication request");
                return null;
            }
            if (StringUtils.isBlank(acsUrl) && acsIndex != null) {
                LOGGER.debug("Locating assertion consumer service url for binding [{}] and index [{}]", acsUrl, acsIndex);
                acsUrl = adapter.getAssertionConsumerServiceFor(binding, acsIndex)
                    .orElseGet(() -> {
                        LOGGER.warn("Unable to locate acs url in for entity [{}] and binding [{}] with index [{}]",
                            adapter.getEntityId(), binding, acsIndex);
                        return null;
                    });
            }

            if (StringUtils.isNotBlank(acsUrl)) {
                LOGGER.debug("Fetched assertion consumer service url [{}] with binding [{}] from authentication request", acsUrl, binding);
                val builder = new AssertionConsumerServiceBuilder();
                val endpoint = builder.buildObject(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
                endpoint.setBinding(binding);
                endpoint.setResponseLocation(acsUrl);
                endpoint.setLocation(acsUrl);
                endpoint.setIndex(acsIndex);
                return endpoint;
            }
        }
        return null;
    }


    /**
     * Determine name id name qualifier string.
     *
     * @param samlRegisteredService   the saml registered service
     * @param samlIdPMetadataResolver the saml id p metadata resolver
     * @return the string
     */
    public static String determineNameIdNameQualifier(final SamlRegisteredService samlRegisteredService,
                                                      final MetadataResolver samlIdPMetadataResolver) {
        if (StringUtils.isNotBlank(samlRegisteredService.getNameIdQualifier())) {
            return samlRegisteredService.getNameIdQualifier();
        }
        val nameQualifier = FunctionUtils.doIf(StringUtils.isNotBlank(samlRegisteredService.getIssuerEntityId()),
                samlRegisteredService::getIssuerEntityId,
                Unchecked.supplier(() -> {
                    val criteriaSet = new CriteriaSet(
                        new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
                        new SamlIdPSamlRegisteredServiceCriterion(samlRegisteredService));
                    LOGGER.trace("Resolving entity id from SAML2 IdP metadata to determine issuer for [{}]", samlRegisteredService.getName());
                    val entityDescriptor = Objects.requireNonNull(samlIdPMetadataResolver.resolveSingle(criteriaSet));
                    return entityDescriptor.getEntityID();
                }))
            .get();
        LOGGER.debug("Using name qualifier [{}] for the Name ID", nameQualifier);
        return nameQualifier;
    }

    /**
     * Does entity descriptor match entity attribute.
     *
     * @param entityDescriptor the entity descriptor
     * @param candidates       the candidates
     * @return true/false
     */
    public static boolean doesEntityDescriptorMatchEntityAttribute(final EntityDescriptor entityDescriptor,
                                                                   final List<MetadataEntityAttributeQuery> candidates) {
        val predicate = buildEntityAttributePredicate(candidates);
        return predicate.test(entityDescriptor);
    }

    /**
     * Build entity attributes predicate.
     *
     * @param candidates the candidates
     * @return the entity attributes predicate
     */
    public static EntityAttributesPredicate buildEntityAttributePredicate(final List<MetadataEntityAttributeQuery> candidates) {
        val attributes = candidates
            .stream()
            .map(entry -> {
                val attr = new EntityAttributesPredicate.Candidate(entry.getName(), entry.getFormat());
                attr.setValues(entry.getValues());
                return attr;
            })
            .toList();
        return new EntityAttributesPredicate(attributes, true);
    }


}


