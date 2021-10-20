package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponseArtifactEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostSimpleSignEncoder;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.CookieUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * This is {@link SamlProfileSaml2ResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SamlProfileSaml2ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {
    private static final long serialVersionUID = 1488837627964481272L;

    public SamlProfileSaml2ResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    @SneakyThrows
    public Response buildResponse(final Assertion assertion,
                                  final AuthenticatedAssertionContext casAssertion,
                                  final RequestAbstractType authnRequest,
                                  final SamlRegisteredService service,
                                  final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                  final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final String binding,
                                  final MessageContext messageContext) throws SamlException {
        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);

        val issuerId = FunctionUtils.doIf(StringUtils.isNotBlank(service.getIssuerEntityId()),
                service::getIssuerEntityId,
                Unchecked.supplier(() -> {
                    val criteriaSet = new CriteriaSet(
                        new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
                        new SamlIdPSamlRegisteredServiceCriterion(service));
                    LOGGER.trace("Resolving entity id from SAML2 IdP metadata to determine issuer for [{}]", service.getName());
                    val entityDescriptor = Objects.requireNonNull(getConfigurationContext().getSamlIdPMetadataResolver().resolveSingle(criteriaSet));
                    return entityDescriptor.getEntityID();
                }))
            .get();

        samlResponse.setIssuer(buildSamlResponseIssuer(issuerId));
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, messageContext), adaptor, binding);
        val location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        samlResponse.setDestination(location);

        if (getConfigurationContext().getCasProperties()
            .getAuthn().getSamlIdp().getCore().isAttributeQueryProfileEnabled()) {
            storeAttributeQueryTicketInRegistry(assertion, request, adaptor);
        }

        val finalAssertion = encryptAssertion(assertion, request, response, service, adaptor);

        if (finalAssertion instanceof EncryptedAssertion) {
            LOGGER.trace("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            LOGGER.trace("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        val status = newStatus(StatusCode.SUCCESS, null);
        samlResponse.setStatus(status);

        SamlUtils.logSamlObject(this.openSamlConfigBean, samlResponse);

        if (service.isSignResponses()) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", adaptor.getEntityId());
            val samlResponseSigned = getConfigurationContext().getSamlObjectSigner()
                .encode(samlResponse, service, adaptor, response, request, binding, authnRequest, messageContext);
            SamlUtils.logSamlObject(openSamlConfigBean, samlResponseSigned);
            return samlResponseSigned;
        }

        return samlResponse;
    }

    @Override
    protected Response encode(final SamlRegisteredService service,
                              final Response samlResponse,
                              final HttpServletResponse httpResponse,
                              final HttpServletRequest httpRequest,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                              final String relayState,
                              final String binding,
                              final RequestAbstractType authnRequest,
                              final AuthenticatedAssertionContext assertion,
                              final MessageContext messageContext) throws SamlException {
        LOGGER.trace("Constructing encoder based on binding [{}] for [{}]", binding, adaptor.getEntityId());
        if (binding.equalsIgnoreCase(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)) {
            val encoder = new SamlResponseArtifactEncoder(
                getConfigurationContext().getVelocityEngineFactory(),
                adaptor, httpRequest, httpResponse,
                getConfigurationContext().getSamlArtifactMap());
            return encoder.encode(authnRequest, samlResponse, relayState, messageContext);
        }

        if (binding.equalsIgnoreCase(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI)) {
            val encoder = new SamlResponsePostSimpleSignEncoder(getConfigurationContext().getVelocityEngineFactory(),
                adaptor, httpResponse, httpRequest);
            return encoder.encode(authnRequest, samlResponse, relayState, messageContext);
        }

        val encoder = new SamlResponsePostEncoder(getConfigurationContext().getVelocityEngineFactory(), adaptor, httpResponse, httpRequest);
        return encoder.encode(authnRequest, samlResponse, relayState, messageContext);
    }

    private void storeAttributeQueryTicketInRegistry(final Assertion assertion, final HttpServletRequest request,
                                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val nameId = (String) request.getAttribute(NameID.class.getName());
        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            getConfigurationContext().getTicketGrantingTicketCookieGenerator(),
            getConfigurationContext().getTicketRegistry(), request);

        val ticket = getConfigurationContext().getSamlAttributeQueryTicketFactory().create(nameId,
            assertion, adaptor.getEntityId(), ticketGrantingTicket);
        getConfigurationContext().getTicketRegistry().addTicket(ticket);
    }
}
