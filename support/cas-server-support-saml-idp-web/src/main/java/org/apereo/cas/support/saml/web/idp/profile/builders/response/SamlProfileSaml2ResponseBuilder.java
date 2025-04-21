package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.services.idp.metadata.MetadataEntityAttributeQuery;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponseArtifactEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostSimpleSignEncoder;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SamlProfileSaml2ResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SamlProfileSaml2ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {

    public SamlProfileSaml2ResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public Response buildResponse(final Optional<Assertion> assertion,
                                  final SamlProfileBuilderContext context) throws Exception {
        val id = '_' + String.valueOf(RandomUtils.nextLong());

        val entityId = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getCore().getEntityId();
        val recipient = getInResponseTo(context.getSamlRequest(), entityId, context.getRegisteredService().isSkipGeneratingResponseInResponseTo());
        val samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), recipient, null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);

        val issuerId = FunctionUtils.doIf(StringUtils.isNotBlank(context.getRegisteredService().getIssuerEntityId()),
                context.getRegisteredService()::getIssuerEntityId,
                Unchecked.supplier(() -> {
                    val criteriaSet = new CriteriaSet(
                        new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
                        new SamlIdPSamlRegisteredServiceCriterion(context.getRegisteredService()));
                    LOGGER.trace("Resolving entity id from SAML2 IdP metadata to determine issuer for [{}]", context.getRegisteredService().getName());
                    val entityDescriptor = Objects.requireNonNull(getConfigurationContext().getSamlIdPMetadataResolver().resolveSingle(criteriaSet));
                    return entityDescriptor.getEntityID();
                }))
            .get();

        samlResponse.setIssuer(buildSamlResponseIssuer(issuerId));
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(context.getSamlRequest(), context.getMessageContext()),
            context.getAdaptor(), context.getBinding());
        val location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        samlResponse.setDestination(location);

        if (getConfigurationContext().getCasProperties()
            .getAuthn().getSamlIdp().getCore().isAttributeQueryProfileEnabled()) {
            storeAttributeQueryTicketInRegistry(assertion, context);
        }

        val finalAssertion = encryptAssertion(assertion, context);
        if (finalAssertion.isPresent()) {
            val result = finalAssertion.get();
            if (result instanceof final EncryptedAssertion encrypted) {
                LOGGER.trace("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
                samlResponse.getEncryptedAssertions().add(encrypted);
            } else if (result instanceof final Assertion nonEncryptedAssertion) {
                LOGGER.trace("Built assertion is not encrypted, so the response will add it to the assertions collection");
                samlResponse.getAssertions().add(nonEncryptedAssertion);
            }
        }

        samlResponse.setStatus(determineResponseStatus(context));

        val customizers = configurationContext.getApplicationContext()
            .getBeansOfType(SamlIdPResponseCustomizer.class).values();
        customizers.stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(customizer -> customizer.customizeResponse(context, this, samlResponse));

        openSamlConfigBean.logObject(samlResponse);

        if (signSamlResponseFor(context)) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", context.getAdaptor().getEntityId());
            val samlResponseSigned = getConfigurationContext().getSamlObjectSigner().encode(samlResponse,
                context.getRegisteredService(), context.getAdaptor(), context.getHttpResponse(), context.getHttpRequest(),
                context.getBinding(), context.getSamlRequest(), context.getMessageContext());
            openSamlConfigBean.logObject(samlResponseSigned);
            return samlResponseSigned;
        }

        return samlResponse;
    }

    protected boolean signSamlResponseFor(final SamlProfileBuilderContext context) {
        return context.getRegisteredService().getSignResponses().isTrue()
            || SamlIdPUtils.doesEntityDescriptorMatchEntityAttribute(context.getAdaptor().getEntityDescriptor(),
            List.of(MetadataEntityAttributeQuery.of(SamlIdPConstants.KnownEntityAttributes.SHIBBOLETH_SIGN_RESPONSES.getName(),
                Attribute.URI_REFERENCE, List.of(Boolean.TRUE.toString()))));
    }

    protected Status determineResponseStatus(final SamlProfileBuilderContext context) {
        if (context.getAuthenticatedAssertion().isEmpty()) {
            if (context.getSamlRequest() instanceof final AuthnRequest authnRequest && authnRequest.isPassive()) {
                val message = """
                    SAML2 authentication request from %s indicated a passive authentication request, \
                    but CAS is unable to satisfy and support this requirement, likely because \
                    no existing single sign-on session is available yet to build the SAML2 response.
                    """.formatted(context.getAdaptor().getEntityId()).stripIndent().trim();
                return newStatus(StatusCode.NO_PASSIVE, message);
            }
            return newStatus(StatusCode.AUTHN_FAILED, null);
        }
        return newStatus(StatusCode.SUCCESS, null);
    }

    @Override
    protected Response encode(final SamlProfileBuilderContext context,
                              final Response samlResponse,
                              final String relayState) {
        LOGGER.trace("Constructing encoder based on binding [{}] for [{}]", context.getBinding(), context.getAdaptor().getEntityId());
        if (context.getBinding().equalsIgnoreCase(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)) {
            val encoder = new SamlResponseArtifactEncoder(
                getConfigurationContext().getVelocityEngineFactory(),
                context.getAdaptor(), context.getHttpRequest(), context.getHttpResponse(),
                getConfigurationContext().getSamlArtifactMap());
            return encoder.encode(context.getSamlRequest(), samlResponse, relayState, context.getMessageContext());
        }

        if (context.getBinding().equalsIgnoreCase(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI)) {
            val encoder = new SamlResponsePostSimpleSignEncoder(getConfigurationContext().getVelocityEngineFactory(),
                context.getAdaptor(), context.getHttpResponse(), context.getHttpRequest());
            return encoder.encode(context.getSamlRequest(), samlResponse, relayState, context.getMessageContext());
        }

        val encoder = new SamlResponsePostEncoder(getConfigurationContext().getVelocityEngineFactory(), context.getAdaptor(), context.getHttpResponse(), context.getHttpRequest());
        return encoder.encode(context.getSamlRequest(), samlResponse, relayState, context.getMessageContext());
    }

    private void storeAttributeQueryTicketInRegistry(final Optional<Assertion> assertion, final SamlProfileBuilderContext context)
        throws Exception {
        val existingQuery = context.getHttpRequest().getAttribute(AttributeQuery.class.getSimpleName());
        if (existingQuery == null && assertion.isPresent()) {
            val nameId = (String) context.getHttpRequest().getAttribute(NameID.class.getName());
            val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                getConfigurationContext().getTicketGrantingTicketCookieGenerator(),
                getConfigurationContext().getTicketRegistry(), context.getHttpRequest());

            if (ticketGrantingTicket != null) {
                val samlAttributeQueryTicketFactory = (SamlAttributeQueryTicketFactory) getConfigurationContext().getTicketFactory().get(SamlAttributeQueryTicket.class);
                val ticket = samlAttributeQueryTicketFactory.create(nameId, assertion.get(), context.getAdaptor().getEntityId(), ticketGrantingTicket);
                getConfigurationContext().getTicketRegistry().addTicket(ticket);
                context.getHttpRequest().setAttribute(SamlAttributeQueryTicket.class.getName(), ticket);
            }
        }
    }
}
