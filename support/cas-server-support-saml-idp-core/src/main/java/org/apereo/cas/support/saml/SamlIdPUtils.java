package org.apereo.cas.support.saml;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.SamlIdPAuthenticationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
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
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
     * Retrieve authn request authn request.
     *
     * @param context            the context
     * @param sessionStore       the session store
     * @param openSamlConfigBean the open saml config bean
     * @param clazz              the clazz
     * @return the request
     */
    public static Optional<Pair<? extends RequestAbstractType, MessageContext>> retrieveSamlRequest(final WebContext context,
                                                                                                    final SessionStore sessionStore,
                                                                                                    final OpenSamlConfigBean openSamlConfigBean,
                                                                                                    final Class<? extends RequestAbstractType> clazz) {
        LOGGER.trace("Retrieving authentication request from scope");
        val authnContext = sessionStore
            .get(context, SamlProtocolConstants.PARAMETER_SAML_REQUEST)
            .map(String.class::cast)
            .map(value -> retrieveSamlRequest(openSamlConfigBean, clazz, value))
            .flatMap(authnRequest -> sessionStore
                .get(context, MessageContext.class.getName())
                .map(String.class::cast)
                .map(result -> SamlIdPAuthenticationContext.decode(result).toMessageContext(authnRequest)));
        return authnContext.map(ctx -> Pair.of((AuthnRequest) ctx.getMessage(), ctx));
    }

    /**
     * Retrieve saml request.
     *
     * @param <T>                the type parameter
     * @param openSamlConfigBean the open saml config bean
     * @param clazz              the clazz
     * @param requestValue       the request value
     * @return the t
     */
    @SneakyThrows
    public static <T extends RequestAbstractType> T retrieveSamlRequest(final OpenSamlConfigBean openSamlConfigBean,
                                                                        final Class<T> clazz, final String requestValue) {
        LOGGER.trace("Retrieving SAML request from [{}]", requestValue);
        try {
            val decodedBytes = Base64Support.decode(requestValue);
            try (val is = new InflaterInputStream(new ByteArrayInputStream(decodedBytes), new Inflater(true))) {
                return clazz.cast(XMLObjectSupport.unmarshallFromInputStream(
                    openSamlConfigBean.getParserPool(), is));
            }
        } catch (final Exception e) {
            val encodedRequest = EncodingUtils.decodeBase64(requestValue.getBytes(StandardCharsets.UTF_8));
            try (val is = new ByteArrayInputStream(encodedRequest)) {
                return clazz.cast(XMLObjectSupport.unmarshallFromInputStream(
                    openSamlConfigBean.getParserPool(), is));
            }
        }
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
                                                            final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                            final String binding) throws SamlException {
        val entityId = adaptor.getEntityId();
        if (!adaptor.containsAssertionConsumerServices()) {
            throw new SamlException("No assertion consumer service could be found for entity " + entityId);
        }

        val peerEntityContext = outboundContext.getSubcontext(SAMLPeerEntityContext.class, true);
        peerEntityContext.setEntityId(entityId);

        val endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
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
                                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
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
                                                                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
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
                    ? AuthnRequest.class.cast(authnRequest).getAssertionConsumerServiceIndex()
                    : null;

                if (StringUtils.isNotBlank(acsUrl) && locations.contains(acsUrl)) {
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
    @SneakyThrows
    public static MetadataResolver getMetadataResolverForAllSamlServices(final ServicesManager servicesManager,
                                                                         final String entityID,
                                                                         final SamlRegisteredServiceCachingMetadataResolver resolver) {

        val registeredServices = servicesManager.findServiceBy(SamlRegisteredService.class::isInstance);
        val chainingMetadataResolver = new ChainingMetadataResolver();

        val resolvers = registeredServices.stream()
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
     * Gets issuer from saml object.
     *
     * @param object the object
     * @return the issuer from saml object
     */
    public static String getIssuerFromSamlObject(final SAMLObject object) {
        if (object instanceof RequestAbstractType) {
            return RequestAbstractType.class.cast(object).getIssuer().getValue();
        }
        if (object instanceof StatusResponseType) {
            return StatusResponseType.class.cast(object).getIssuer().getValue();
        }
        if (object instanceof Assertion) {
            return Assertion.class.cast(object).getIssuer().getValue();
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
        if (authnRequest instanceof AuthnRequest) {
            return Optional.ofNullable(AuthnRequest.class.cast(authnRequest).getNameIDPolicy());
        }
        return Optional.empty();
    }

    private static AssertionConsumerService getAssertionConsumerServiceFromRequest(final RequestAbstractType request,
                                                                                   final String binding,
                                                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adapter) {
        if (request instanceof AuthnRequest) {
            val authnRequest = AuthnRequest.class.cast(request);
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
     * Store saml request.
     *
     * @param webContext         the web context
     * @param openSamlConfigBean the open saml config bean
     * @param sessionStore       the session store
     * @param context            the context
     * @throws Exception the exception
     */
    public static void storeSamlRequest(final JEEContext webContext,
                                        final OpenSamlConfigBean openSamlConfigBean,
                                        final SessionStore sessionStore,
                                        final Pair<? extends SignableSAMLObject, MessageContext> context) throws Exception {
        val authnRequest = (AuthnRequest) context.getLeft();
        val messageContext = context.getValue();
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest)) {
            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            sessionStore.set(webContext, SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest);
            sessionStore.set(webContext, SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, SAMLBindingSupport.getRelayState(messageContext));

            val authnContext = SamlIdPAuthenticationContext.from(messageContext).encode();
            sessionStore.set(webContext, MessageContext.class.getName(), authnContext);
        }
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
}


