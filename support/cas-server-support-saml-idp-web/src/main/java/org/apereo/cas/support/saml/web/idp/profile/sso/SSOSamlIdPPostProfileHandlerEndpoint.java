package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RestControllerEndpoint(id = "samlPostProfileResponse", enableByDefault = false)
public class SSOSamlIdPPostProfileHandlerEndpoint extends BaseCasActuatorEndpoint {
    private final ServicesManager servicesManager;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final PrincipalFactory principalFactory;

    private final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder;

    private final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    private final AbstractSaml20ObjectBuilder saml20ObjectBuilder;


    public SSOSamlIdPPostProfileHandlerEndpoint(final CasConfigurationProperties casProperties,
                                                final ServicesManager servicesManager,
                                                final AuthenticationSystemSupport authenticationSystemSupport,
                                                final ServiceFactory<WebApplicationService> serviceFactory,
                                                final PrincipalFactory principalFactory,
                                                final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder,
                                                final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
                                                final AbstractSaml20ObjectBuilder saml20ObjectBuilder) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.serviceFactory = serviceFactory;
        this.principalFactory = principalFactory;
        this.responseBuilder = responseBuilder;
        this.defaultSamlRegisteredServiceCachingMetadataResolver = defaultSamlRegisteredServiceCachingMetadataResolver;
        this.saml20ObjectBuilder = saml20ObjectBuilder;
    }

    /**
     * Produce response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<Object> produce(final HttpServletRequest request, final HttpServletResponse response) {
        val username = request.getParameter("username");
        val password = request.getParameter("password");
        val entityId = request.getParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);

        try {
            val selectedService = this.serviceFactory.createService(entityId);
            val registeredService = this.servicesManager.findServiceBy(selectedService, SamlRegisteredService.class);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            val authnRequest = new AuthnRequestBuilder().buildObject();
            authnRequest.setIssuer(saml20ObjectBuilder.newIssuer(entityId));

            val adaptorResult = SamlRegisteredServiceServiceProviderMetadataFacade.get(
                defaultSamlRegisteredServiceCachingMetadataResolver, registeredService, entityId);
            if (adaptorResult.isPresent()) {
                val adaptor = adaptorResult.get();
                val messageContext = new MessageContext();
                val scratch = messageContext.getSubcontext(ScratchContext.class, true);
                val map = (Map) Objects.requireNonNull(scratch).getMap();
                map.put(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.FALSE);
                val assertion = getAssertion(username, password, entityId);
                val object = this.responseBuilder.build(authnRequest, request, response, assertion,
                    registeredService, adaptor, SAMLConstants.SAML2_POST_BINDING_URI, messageContext);
                val encoded = SamlUtils.transformSamlObject(saml20ObjectBuilder.getOpenSamlConfigBean(), object).toString();
                return new ResponseEntity<>(encoded, HttpStatus.OK);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Assertion getAssertion(final String username,
                                   final String password,
                                   final String entityId) {
        val selectedService = this.serviceFactory.createService(entityId);
        val registeredService = this.servicesManager.findServiceBy(selectedService, SamlRegisteredService.class);

        val credential = new UsernamePasswordCredential(username, password);
        val result = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(selectedService, credential);
        val authentication = result.getAuthentication();

        val principal = authentication.getPrincipal();
        val attributesToRelease = registeredService.getAttributeReleasePolicy().getAttributes(principal, selectedService, registeredService);
        val builder = DefaultAuthenticationBuilder.of(
            principal,
            this.principalFactory,
            attributesToRelease,
            selectedService,
            registeredService,
            authentication);

        val finalAuthentication = builder.build();
        val authnPrincipal = finalAuthentication.getPrincipal();
        val p = new AttributePrincipalImpl(authnPrincipal.getId(), (Map) authnPrincipal.getAttributes());

        return new AssertionImpl(p, DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC)),
            null, DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC)),
            (Map) finalAuthentication.getAttributes());
    }
}
