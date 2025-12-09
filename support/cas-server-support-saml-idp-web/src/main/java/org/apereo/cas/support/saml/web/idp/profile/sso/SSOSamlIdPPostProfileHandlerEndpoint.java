package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Endpoint(id = "samlPostProfileResponse", defaultAccess = Access.NONE)
public class SSOSamlIdPPostProfileHandlerEndpoint extends BaseCasRestActuatorEndpoint {

    private final ServicesManager servicesManager;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final PrincipalFactory principalFactory;

    private final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder;

    private final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    private final AbstractSaml20ObjectBuilder saml20ObjectBuilder;

    private final PrincipalResolver principalResolver;

    private final MetadataResolver samlIdPMetadataResolver;

    public SSOSamlIdPPostProfileHandlerEndpoint(final CasConfigurationProperties casProperties,
                                                final ConfigurableApplicationContext applicationContext,
                                                final ServicesManager servicesManager,
                                                final AuthenticationSystemSupport authenticationSystemSupport,
                                                final ServiceFactory<WebApplicationService> serviceFactory,
                                                final PrincipalFactory principalFactory,
                                                final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder,
                                                final SamlRegisteredServiceCachingMetadataResolver cachingMetadataResolver,
                                                final AbstractSaml20ObjectBuilder saml20ObjectBuilder,
                                                final PrincipalResolver principalResolver,
                                                final MetadataResolver samlIdPMetadataResolver) {
        super(casProperties, applicationContext);
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
        this.responseBuilder = responseBuilder;
        this.defaultSamlRegisteredServiceCachingMetadataResolver = cachingMetadataResolver;
        this.saml20ObjectBuilder = saml20ObjectBuilder;
        this.principalResolver = principalResolver;
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
    }

    /**
     * Produce response entity.
     *
     * @param request     the request
     * @param response    the response
     * @param samlRequest the saml request
     * @return the response entity
     */
    @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    @Operation(summary = "Produce SAML2 response entity", parameters = {
        @Parameter(name = "username", required = true, description = "The username to authenticate"),
        @Parameter(name = "password", required = false, description = "The password to authenticate"),
        @Parameter(name = SamlProtocolConstants.PARAMETER_ENTITY_ID, required = true, description = "The entity id"),
        @Parameter(name = "encrypt", schema = @Schema(type = "boolean"), description = "Whether to encrypt the response")
    })
    public ResponseEntity<@NonNull Object> producePost(final HttpServletRequest request,
                                                       final HttpServletResponse response,
                                                       @ModelAttribute
                                              final SamlRequest samlRequest) {
        return produce(request, response, samlRequest);
    }


    /**
     * Produce logout request post.
     *
     * @param entityId the entity id
     * @param response the response
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(value = "/logout/post", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Produce SAML2 logout request for the given SAML2 SP",
               parameters = @Parameter(name = SamlProtocolConstants.PARAMETER_ENTITY_ID, required = true, description = "The entity id"))
    public ResponseEntity<@NonNull Object> produceLogoutRequestPost(
        @RequestParam(SamlProtocolConstants.PARAMETER_ENTITY_ID) final String entityId,
        final HttpServletResponse response) throws Exception {
        val selectedService = serviceFactory.createService(entityId);
        val registeredService = servicesManager.findServiceBy(selectedService, SamlRegisteredService.class);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(selectedService, registeredService);

        val logoutRequest = saml20ObjectBuilder.newSamlObject(LogoutRequest.class);
        logoutRequest.setID(RandomUtils.randomAlphabetic(4));
        val issuer = saml20ObjectBuilder.newSamlObject(Issuer.class);
        issuer.setValue(entityId);

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getCore().getEntityId()));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new BindingCriterion(List.of(SAMLConstants.SAML2_POST_BINDING_URI)));

        val result = samlIdPMetadataResolver.resolveSingle(criteriaSet);
        val sloEndpointDestination = result.getIDPSSODescriptor(SAMLConstants.SAML20P_NS)
            .getEndpoints(SingleLogoutService.DEFAULT_ELEMENT_NAME)
            .stream()
            .filter(endpoint -> SAMLConstants.SAML2_POST_BINDING_URI.equals(endpoint.getBinding()))
            .findFirst()
            .orElseThrow()
            .getLocation();

        val nameId = saml20ObjectBuilder.newSamlObject(NameID.class);
        nameId.setValue(UUID.randomUUID().toString());
        logoutRequest.setNameID(nameId);

        logoutRequest.setIssuer(issuer);
        logoutRequest.setDestination(sloEndpointDestination);
        logoutRequest.setIssueInstant(Instant.now(Clock.systemUTC()).minusSeconds(10));

        val encoder = new HTTPPostEncoder();
        encoder.setVelocityEngine(saml20ObjectBuilder.getOpenSamlConfigBean().getVelocityEngine());
        encoder.setHttpServletResponseSupplier(() -> response);
        val messageContext = new MessageContext();
        SAMLBindingSupport.setRelayState(messageContext, UUID.randomUUID().toString());
        val peerEntityContext = messageContext.ensureSubcontext(SAMLPeerEntityContext.class);
        val endpointContext = peerEntityContext.ensureSubcontext(SAMLEndpointContext.class);

        val endpoint = saml20ObjectBuilder.newSamlObject(SingleSignOnService.class);
        endpoint.setLocation(sloEndpointDestination);
        endpointContext.setEndpoint(endpoint);

        val encodedRequest = EncodingUtils.encodeBase64(SamlUtils.transformSamlObject(
            saml20ObjectBuilder.getOpenSamlConfigBean(), logoutRequest, true).toString());
        response.setHeader("LogoutRequest", encodedRequest);
        
        messageContext.setMessage(logoutRequest);
        encoder.setMessageContext(messageContext);
        encoder.initialize();
        encoder.encode();

        return ResponseEntity.ok().build();
    }

    private ResponseEntity<@NonNull Object> produce(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final SamlRequest samlRequest) {
        try {
            val selectedService = serviceFactory.createService(samlRequest.getEntityId());
            val registeredService = servicesManager.findServiceBy(selectedService, SamlRegisteredService.class);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(selectedService, registeredService);

            val loadedService = new SamlRegisteredService();
            BeanUtils.copyProperties(registeredService, loadedService);
            loadedService.setEncryptAssertions(samlRequest.isEncrypt());
            loadedService.setEncryptAttributes(samlRequest.isEncrypt());

            val authnRequest = new AuthnRequestBuilder().buildObject();
            authnRequest.setIssuer(saml20ObjectBuilder.newIssuer(samlRequest.getEntityId()));

            val result = SamlRegisteredServiceMetadataAdaptor.get(defaultSamlRegisteredServiceCachingMetadataResolver, loadedService, samlRequest.getEntityId());
            return result
                .map(Unchecked.function(adaptor -> {
                    val messageContext = new MessageContext();
                    val scratch = messageContext.ensureSubcontext(ScratchContext.class);
                    val map = (Map) Objects.requireNonNull(scratch).getMap();
                    map.put(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.FALSE);
                    val assertion = getAssertion(samlRequest);
                    val buildContext = SamlProfileBuilderContext.builder()
                        .samlRequest(authnRequest)
                        .httpRequest(request)
                        .httpResponse(response)
                        .authenticatedAssertion(Optional.of(assertion))
                        .registeredService(loadedService)
                        .adaptor(adaptor)
                        .binding(SAMLConstants.SAML2_POST_BINDING_URI)
                        .messageContext(messageContext)
                        .build();
                    val object = responseBuilder.build(buildContext);
                    val encoded = SamlUtils.transformSamlObject(saml20ObjectBuilder.getOpenSamlConfigBean(), object, true).toString();
                    return new ResponseEntity<@NonNull Object>(encoded, HttpStatus.OK);
                }))
                .orElseThrow(() -> new SamlException("Unable to locate " + samlRequest.getEntityId()));
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private AuthenticatedAssertionContext getAssertion(final SamlRequest samlRequest) throws Throwable {
        val selectedService = serviceFactory.createService(samlRequest.getEntityId());
        val registeredService = servicesManager.findServiceBy(selectedService, SamlRegisteredService.class);

        val authentication = authenticateRequest(samlRequest, selectedService);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(saml20ObjectBuilder.getOpenSamlConfigBean().getApplicationContext())
            .service(selectedService)
            .principal(authentication.getPrincipal())
            .build();
        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(context);
        val builder = DefaultAuthenticationBuilder.of(
            context.getApplicationContext(), authentication.getPrincipal(),
            principalFactory, attributesToRelease,
            selectedService, registeredService, authentication);

        val finalAuthentication = builder.build();
        val authnPrincipal = finalAuthentication.getPrincipal();
        return AuthenticatedAssertionContext.builder()
            .name(authnPrincipal.getId())
            .attributes(CollectionUtils.merge(authnPrincipal.getAttributes(), finalAuthentication.getAttributes()))
            .build();
    }

    private Authentication authenticateRequest(final SamlRequest samlRequest, final WebApplicationService selectedService) throws Throwable {
        if (StringUtils.isNotBlank(samlRequest.getPassword())) {
            val credential = new UsernamePasswordCredential(samlRequest.getUsername(), samlRequest.getPassword());
            val result = authenticationSystemSupport.finalizeAuthenticationTransaction(selectedService, credential);
            return result.getAuthentication();
        }
        val principal = principalResolver.resolve(new BasicIdentifiableCredential(samlRequest.getUsername()),
            Optional.of(principalFactory.createPrincipal(samlRequest.getUsername())),
            Optional.empty(), Optional.of(selectedService));
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    public static class SamlRequest implements Serializable {
        @Serial
        private static final long serialVersionUID = 9132411807103771828L;

        private String username;

        private String password;

        private String entityId;

        private boolean encrypt;
    }
}
